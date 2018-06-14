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

    private static File generatecsv1(ArrayList slct, Map words)
    {
        ArrayList<ArrayList<Object>> dataList = new ArrayList<>();
        ArrayList temp;
        for (int i = 0; i < slct.size(); i++)
        {
            temp = new ArrayList();
            temp.add(i);
            temp.add(slct.get(i));
            if (words.containsKey(slct.get(i)))
                temp.add(words.get(slct.get(i)));
            else
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
            //writterSettings.setHeaders(title);
            CsvWriter writer = new CsvWriter(new FileWriter(outFile),writterSettings);
            writer.writeHeaders(title);
            writer.writeRowsAndClose(dataList);//记住最后要close
            System.out.println("创建关键词文件成功");
        }catch (IOException e){
            System.out.println("Failed to create CSV file for writing data");
        }

        return outFile;
    }

    private static File generatecsv2(ArrayList slct, Map pagewords, Map words)
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
                    if (words.containsKey(slct.get(i)) && t.containsKey(slct.get(j)))
                        count += (int)t.get(slct.get(j))-1;//注意这里应该-1，因为如果title为i的话，那么刚刚其实已经算过了一次（等于说刚才算的是存在与否，现在算的是次数）
                }

                temp.add(count);

                dataList.add(temp);
            }
        String outpath = "E://study//junior//XML";
        String [] title = {"id1","id2","co_times"};
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
            //writterSettings.setHeaders(title);
            CsvWriter writer = new CsvWriter(new FileWriter(outFile),writterSettings);
            writer.writeHeaders(title);
            writer.writeRowsAndClose(dataList);//记住最后要close
            System.out.println("创建关键词关联文件成功");
        }catch (IOException e){
            System.out.println("Failed to create CSV file for writing data");
        }

        return outFile;
    }

    public static String gettimelabel(Node node){
        Node page = node.getParentNode();//先找到page结点
        NodeList pagelabel = page.getChildNodes();//找到page的所有子节点
        NodeList tempchildlist;
        Node temppart;


        for (int i = 0; i < pagelabel.getLength(); i++)//找label为template的元素
            if(pagelabel.item(i).getNodeName().equals("template")) {
                tempchildlist = pagelabel.item(i).getChildNodes();//template的子节点
                for (int j = 0; j < tempchildlist.getLength(); j++)//找template的孩子节点
                {
                    if (tempchildlist.item(j).getNodeName().equals("title")) {
                        if (!tempchildlist.item(j).getChildNodes().item(0).getNodeValue().equals("Use dmy dates")//这里直接取了item(0)因为title只有一个文本节点
                                && !tempchildlist.item(j).getChildNodes().item(0).getNodeValue().equals("Use mdy dates"))
                            break;//如果template的title不是这两类则这个template不符合条件
                        else {//不然这个就是我们要找的template
                            for (int k = 0; k < tempchildlist.getLength(); k++)//重新对这个template遍历，找part label
                            {
                                if (tempchildlist.item(k).getNodeName().equals("part")) {
                                    temppart = tempchildlist.item(k);
                                    for (int l = 0; l < temppart.getChildNodes().getLength(); l++)//找part的子节点
                                    {
                                        if (temppart.getChildNodes().item(l).getNodeName().equals("value")) {
                                            return temppart.getChildNodes().item(l).getChildNodes().item(0).getNodeValue();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
        }


        return "";
    }


    public static void main(String[] args) throws Exception {
        Map pagewords = new HashMap();//用来存储每一个paper的情况，value是另一个map
        Map times ;//用来存储一个paper中words出现的情况
        Map words = new HashMap();//存10个pagetitle

        List<String> list;
        ArrayList slct = new ArrayList<>();

        document = db.parse("E:\\study\\junior\\XML\\homework\\work1.xml");
        NodeList bookList = document.getElementsByTagName("pagetitle"); //booklist是pagetitles的一个list
        //建立pagetitle的关键词集合，以及在100个keywords集合中先加入pagetitle的10个
        for (int i = 0; i < bookList.getLength(); i++) {//10个page
            Node node = bookList.item(i);
            NodeList nodeList = node.getChildNodes();
            for (int j = 0; j < nodeList.getLength(); j++) {//nodelist其实只有一个元素，就是pagetitle的文本节点
                    //找到pagetitle的time label
                String time = gettimelabel(nodeList.item(j).getParentNode());//找到time

                slct.add(nodeList.item(j).getNodeValue());
                words.put(nodeList.item(j).getNodeValue(), time);

            }
        }

        //再处理文本节点的关键词
        for (int i = 0; i < bookList.getLength(); i++) {//10个pagetitle
            Node page = bookList.item(i).getParentNode();
            NodeList nodeList1 = page.getChildNodes();//当前Page的Nodelist
            times = new HashMap();//初始化times

            //先将Pagetitle放进去
            times.put(bookList.item(i).getChildNodes().item(0).getNodeValue(),1);


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
                            if (slct.size() < 100 && !slct.contains(list.get(k)))
                                slct.add(list.get(k));
                        }
                    }
                }
            }
            pagewords.put(i, times);
        }
        //生成关键词表格
        generatecsv1(slct,words);
        //生成对关系表格
        generatecsv2(slct,pagewords,words);

    }

}
