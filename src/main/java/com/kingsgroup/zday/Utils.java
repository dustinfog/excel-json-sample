package com.kingsgroup.zday;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Utils {

    public static final Properties properties = new Properties();

    static {
        try {
            properties.load(new FileInputStream(System.getProperty("user.dir") + File.separator + "config.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getCurrPathExcel() {
        String workspaceRoot = properties.getProperty("workspace.root");
        return workspaceRoot + File.separator + properties.getProperty("workspace.dir.excel");
    }

    public static String getCurrPathJson() {
        String workspaceRoot = properties.getProperty("workspace.root");
        return workspaceRoot + File.separator + properties.getProperty("workspace.dir.json");
    }

    public static String getCurrPathGit() {
        String workspaceRoot = properties.getProperty("workspace.root");
        return workspaceRoot + File.separator + properties.getProperty("workspace.dir.git");
    }

    public static String[] getWatchPaths() {
        return new String[]{getCurrPathGit(), getCurrPathExcel(), getCurrPathJson()};
    }

    public static String getFileName(String fileName) {
        if (fileName.endsWith(".xlsx"))
            return fileName.replace(".xlsx","");
        return fileName.replace(".json", "");
    }

    public static String readJsonFile(String fileName) {
        String jsonStr;
        try {
            File jsonFile = new File(fileName);
            FileReader fileReader = new FileReader(jsonFile);

            Reader reader = new InputStreamReader(new FileInputStream(jsonFile), StandardCharsets.UTF_8);
            int ch;
            StringBuilder sb = new StringBuilder();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            fileReader.close();
            reader.close();
            jsonStr = sb.toString();
            return jsonStr;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean transferJsonToExcel(String jsonFilePath, String excelPath, String fileName) throws IOException {
        File file = new File(excelPath);
        if (file.exists() && !canBeWritten(file)) {
            Log.log.error("file can not write ");
            return false;
        }
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        String jsonStr = readJsonFile(jsonFilePath);
        ObjectMapper mapper = new ObjectMapper();
        assert jsonStr != null;
        JsonNode node = mapper.readTree(jsonStr);
        LinkedHashMap<String, Object> headerJsonData = mapper.convertValue(node.path("header"),
                new TypeReference<LinkedHashMap<String, Object>>() {
        });

        XSSFWorkbook workbook = new XSSFWorkbook();
        String sheetName = fileName != null ? fileName : "mysheet";
        XSSFSheet sheet = workbook.createSheet(sheetName);
        AtomicInteger roleNo = new AtomicInteger();
        AtomicInteger rowNo = new AtomicInteger();

        XSSFRow row = sheet.createRow(roleNo.getAndIncrement());
        Map<Integer, String> headerMap = new HashMap<>();
        if (headerJsonData != null) {
            headerJsonData.forEach((k, v) -> {
                XSSFCell cell = row.createCell(rowNo.get());
                cell.setCellValue(v.toString());
                headerMap.put(rowNo.get(), k);
                rowNo.getAndIncrement();
            });
        }

        List<Map<String, String>> rowJsonData = mapper.convertValue(node.path("rows"),
                new TypeReference<List<Map<String, String>>>() {
        });
        rowJsonData.forEach((map) -> {
            XSSFRow r = sheet.createRow(roleNo.getAndIncrement());
            for (int i = 0; i < rowNo.get(); ++i) {
                XSSFCell cell = r.createCell(i);
                Object value = map.get(headerMap.get(i));
                cell.setCellValue(value == null ? "" : value.toString());
            }
        });
        FileOutputStream output = new FileOutputStream(excelPath);
        workbook.write(output);
        workbook.close();
        output.flush();
        output.close();
        return true;
    }

    public static String calcFileMD5(File file) throws IOException {
        if (!file.isFile()) {
            return "";
        }
        return DigestUtils.md5Hex(new FileInputStream(file));
    }

    public static String getGitInfo() throws IOException {
        String gitHeadPath  = getCurrPathGit() + File.separator + "HEAD";

        FileInputStream inputStream = new FileInputStream(gitHeadPath);
        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(reader);
        String line;
        while((line = br.readLine()) != null) {
            String[] str = line.split("\\/");
            if (str.length > 0) {
                line = str[str.length - 1];
                break;
            }
        }
        //System.out.println(line);
        return line;
    }

    public static String execGitCmd(String cmd) throws IOException {
        Process process = Runtime.getRuntime().exec(cmd, null, new File(getCurrPathGit()));
        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    public static boolean canBeWritten(File file) throws IOException {
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(file, "rw");
            return true;
        } catch (Exception e) {
            Log.log.error("skipFile "+file.getName()+"  can not written");
        } finally {
            if (randomAccessFile != null) {
                randomAccessFile.close();
            }
        }
        return false;
    }

    public static String getGitHead() throws IOException {
        String command = "git symbolic-ref --short -q HEAD";
        return execGitCmd(command);
    }

    /**
     * 检查git冲突
     * @return true 有冲突 false 无冲突
     * @throws IOException
     */
    public static boolean checkGitConflict() throws IOException {
        String command = "git diff --check";
        String result = execGitCmd(command);
        return !"".equals(result);
    }

    public static void main(String[] args) throws IOException {
        getGitInfo();
        getGitHead();
        System.out.println(checkGitConflict());
    }
}
