package com.kingsgroup.zday;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ObjectSerialize {

    public static byte[] serialize(Object object) {
        ObjectOutputStream outputStream;
        ByteArrayOutputStream byteArrayOutputStream;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            outputStream = new ObjectOutputStream(byteArrayOutputStream);
            outputStream.writeObject(object);
            return byteArrayOutputStream.toByteArray();

        } catch (Exception e) {
            Log.log.error("Object:"+object.toString()+" serialize fail!!");
        }
        return null;
    }

    public static Object unSerialize(byte[] bytes) {
        ByteArrayInputStream byteArrayInputStream;
        try {
            byteArrayInputStream = new ByteArrayInputStream(bytes);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            return objectInputStream.readObject();
        } catch (Exception e) {
            Log.log.error("bytes unSerialize fail");
        }
        return null;
    }

    public static void main(String[] args) {
        Record record = new Record("test", "dsdsdsdsdsd");

        record.setGitHeadInfo("master");
        System.out.println(record.toString());
        byte[] bytes = serialize(record);
        Record record1 = (Record) unSerialize(bytes);

        ExcelChangeRecords excelFileRecord = new ExcelChangeRecords("master");
        excelFileRecord.records.put(record.fileFullName, BasicEvent.ENTRY_MODIFY);
        System.out.println(record.toString());

        byte[] bytes1 = serialize(excelFileRecord);
        ExcelChangeRecords record2 = (ExcelChangeRecords) unSerialize(bytes1);
        assert record2 != null;
        System.out.println(record2.toString());
    }

}


