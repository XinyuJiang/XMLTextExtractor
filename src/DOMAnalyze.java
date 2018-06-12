package test;



import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DOMAnalyze {

    private static DocumentBuilderFactory dbFactory = null;
    private static DocumentBuilder db = null;
    private static Document document = null;
    static{
        try {
            dbFactory = DocumentBuilderFactory.newInstance();
            db = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    //提取文本中的关键词
    private static ArrayList splitsentence(String str)
    {
        ArrayList<String>  word = new ArrayList<String>();
        int m= 0 ,n = 0;
        int count = 0;//解决括号嵌套的问题
        for(int i = 0;i<str.length() - 1; i++){
            if(str.charAt(i) == '['&& str.charAt(i+1) == '['){
                if(count == 0){
                    m = i;
                }
                count++;
            }
            if(str.charAt(i) == ']' && str.charAt(i+1) ==']'){
                count--;
                if(count == 0){
                    n = i;
                    word.add(str.substring(m+2 , n));
                }
            }
        }
        return word;
    }

    private static File generatecsv1(ArrayList slct)
    {
        ArrayList<ArrayList<Object>> dataList = new ArrayList<>();
        ArrayList temp;
        for (int i = 0; i < slct.size(); i++)
        {
            temp = new ArrayList();
            temp.add(i);
            temp.add(slct.get(i));
            temp.add("");
            dataList.add(temp);
        }
        String outpath = "E://study//junior//XML";
        String [] title = {"id","name","time"};
        String filename = "mission1";
        File outFile = null;
        try{
            //创建文件
            outFile = new File(outpath + File.separator + filename + ".csv");
            File parent = outFile.getParentFile();
            if (parent != null && !parent.exists())
            {
                parent.mkdirs();
            }
            outFile.createNewFile();

            //写出csv文件,使用了一个相关的开源库，uniVocity-parsers
            CsvWriterSettings writterSettings = new CsvWriterSettings();
            writterSettings.setHeaders(title);
            CsvWriter writer = new CsvWriter(new FileWriter(outFile),writterSettings);
            writer.writeRowsAndClose(dataList);//记住最后要close
        }catch (IOException e){
            System.out.println("Failed to create CSV file for writing data");
        }

        return outFile;
    }

    private static File generatecsv2(ArrayList slct, Map pagewords, ArrayList words)
    {
        ArrayList<ArrayList<Object>> dataList = new ArrayList<>();
        ArrayList temp;
        Map t = new HashMap();
        int count;
        for (int i = 0; i < slct.size(); i++)
            for (int j = i+1; j < slct.size(); j++)
            {
                //初始化计数次数
                count = 0;
                temp = new ArrayList();
                temp.add(slct.get(i));
                temp.add(slct.get(j));
                //计算共同出现次数
                for (int k = 0 ; k < pagewords.size(); k++)
                {
                    t = (Map)pagewords.get(k);
                    if (t.containsKey(slct.get(i)) && t.containsKey(slct.get(j)))
                        count++;
                    if (words.contains(slct.get(i)) && t.containsKey(slct.get(j)))
                        count += (int)t.get(slct.get(j))-1;//注意这里应该-1，因为如果title为i的话，那么刚刚其实已经算过了一次（等于说刚才算的是存在与否，现在算的是次数）
                }

                temp.add(count);

                dataList.add(temp);
            }
        String outpath = "E://study//junior//XML";
        String [] title = {"id","name","time"};
        String filename = "mission2";
        File outFile = null;
        try{
            //创建文件
            outFile = new File(outpath + File.separator + filename + ".csv");
            File parent = outFile.getParentFile();
            if (parent != null && !parent.exists())
            {
                parent.mkdirs();
            }
            outFile.createNewFile();

            //写出csv文件,使用了一个相关的开源库，uniVocity-parsers
            CsvWriterSettings writterSettings = new CsvWriterSettings();
            writterSettings.setHeaders(title);
            CsvWriter writer = new CsvWriter(new FileWriter(outFile),writterSettings);
            writer.writeRowsAndClose(dataList);//记住最后要close
        }catch (IOException e){
            System.out.println("Failed to create CSV file for writing data");
        }

        return outFile;
    }


    public static void main(String[] args) throws Exception {
        Map pagewords = new HashMap();//用来存储每一个paper的情况，value是另一个map
        Map times ;//用来存储一个paper中words出现的情况
        ArrayList<String> words = new ArrayList<String>();

        List<String> list;
        ArrayList slct = new ArrayList<>();

        document = db.parse("E:\\study\\junior\\XML\\homework\\work1.xml");
        NodeList bookList = document.getElementsByTagName("pagetitle"); //booklist是pagetitles的一个list
        for (int i = 0; i < bookList.getLength(); i++) {//10个page
            times = new HashMap();//初始化times
            Node node = bookList.item(i);
            //System.out.println(node.getNodeValue());//null
            //System.out.println(node.getNodeName());//pagetitle
            NodeList nodeList = node.getChildNodes();
            //先处理pagetitle中的关键词
            for(int j = 0; j < nodeList.getLength(); j ++) {//nodelist其实只有一个元素，就是pagetitle的文本节点
                //如果times中已经有这个关键词了，那么只要在当前value上+1即可
                if (times.containsKey(nodeList.item(j).getNodeValue())) {
                    times.put(nodeList.item(j).getNodeValue(), (int) times.get(nodeList.item(j).getNodeValue()) + 1);//先将pagetitle的标题放进去
                    //将pagetitle放入我们钦定的关键词中
                }
                    //不然就新建一个关键词
                else{
                    times.put(nodeList.item(j).getNodeValue(), 1);
                    if (slct.size() < 100)
                        slct.add(nodeList.item(j).getNodeValue());
                    words.add(nodeList.item(j).getNodeValue());

                }
            }
            //pagetitle处理完了，再处理page的文本节点中的关键词
            Node page = bookList.item(i).getParentNode();
            NodeList nodeList1 = page.getChildNodes();//当前Page的Nodelist
            for(int l = 0; l < nodeList1.getLength(); l ++){
                //遍历page目录下的所有直接子节点
                if(nodeList1.item(l).getNodeType() == 3&&nodeList1.item(l).getNodeValue().contains("[["))//tyle3表示文本节点
                {
                    list = splitsentence(nodeList1.item(l).getNodeValue());
                    for(int k = 0; k < list.size(); k++)
                    {
                        if (times.containsKey(list.get(k))) {
                            times.put(list.get(k), (int) times.get(list.get(k)) + 1);
                        }

                        else {
                            times.put(list.get(k), 1);
                            if (slct.size() < 100)
                                slct.add(list.get(k));
                        }
                    }
                }
            }
            pagewords.put(i,times);
        }
        //生成关键词表格
        generatecsv1(slct);
        //生成对关系表格
        generatecsv2(slct,pagewords,words);

    }

}
