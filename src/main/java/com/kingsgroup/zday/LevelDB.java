package com.kingsgroup.zday;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBFactory;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LevelDB {
    private  DB db = null;
    private String dbFile = "db/record.db";
    private String charset = "utf-8";

    private static LevelDB instance = new LevelDB();

    private LevelDB() {}

    public static LevelDB getInstance() {
        return instance;
    }

    public void initLevelDB() {
        DBFactory dbFactory = new Iq80DBFactory();
        Options options = new Options();
        options.createIfMissing(true);
        try {
            this.db = dbFactory.open(new File(dbFile), options);
        } catch (IOException e) {
            throw new RuntimeException("leveldb 启动失败");
        }
        Log.log.info("leveldb started");
    }

    public void insert(String key, Object value){
        db.put(Iq80DBFactory.bytes(key), ObjectSerialize.serialize(value));
    }

    public void update(String key, Object value) {
        insert(key, value);
    }

    public Object get(String fileName) {
        byte[] bytes = db.get(Iq80DBFactory.bytes(fileName));
        if (null == bytes || bytes.length == 0) {
            return null;
        }
        return ObjectSerialize.unSerialize(bytes);
    }

    public List<String> getAllKey() {
        List<String> keyList = new ArrayList<>();
        DBIterator iterator = null;
        try {
            iterator = db.iterator();
            while (iterator.hasNext()) {
                Map.Entry<byte[], byte[]> entry = iterator.next();
                String key = new String(entry.getValue(), charset);
                keyList.add(key);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            if (null != iterator) {
                try {
                    iterator.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return keyList;
    }

    public void delete(String key) {
        db.delete(Iq80DBFactory.bytes(key));
    }


    public void closeDB() throws IOException {
        if (db != null) {
            db.close();
        }
    }
}
