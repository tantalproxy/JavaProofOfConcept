package org.monolith.exp;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

import java.sql.Wrapper;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;

import org.joda.time.DateTime;
import org.monolith.exp.WritableObject;
import redis.clients.jedis.Jedis;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class App {

    public static void main(String[] args) {

        //UUID uid = UUID.randomUUID();

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("PU1");

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        WritableObject o = new WritableObject();

        try {
            o = em.find(WritableObject.class, 1L);
            o.toString();
        } catch (NullPointerException e) {
            
            o = new WritableObject();
            o.setId(1L);
            o.setName("ABC");
            o.setDate(new Date());

            tx.begin();
            em.persist(o);
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            em.close();
            emf.close();
        }

        String uid = "1";
        String jsonData = JsonWriter.objectToJson(o);
        Jedis client = new Jedis("localhost", 6379);
        Object uncastObject = client.get("TEST#" + uid);

        if (uncastObject == null) {
            System.out.println("NULL FIRST READ FROM REDIS->" + "(" +  uid + ")" + "=" + "null");
        } else {
            System.out.println("PRE READ FROM REDIS->" + "(" + uid + ")" + "=" + uncastObject);
            Object readedObject = JsonReader.jsonToJava((String) uncastObject);
            WritableObject preWriteObject = (WritableObject) readedObject;
            System.out.println("PRE WRITE FROM REDIS->" + "(" +  uid + ")" + "=" + preWriteObject.toString());
        }

        client.set("TEST#" + uid, jsonData);

        String stringObject = client.get("TEST#" + uid);

        if (stringObject == null) {
            stringObject = "null";
        }

        System.out.println("POST READ FROM REDIS->" + "(" +  uid + ")" + "=" + stringObject);
        WritableObject redisObject = (WritableObject) JsonReader.jsonToJava(client.get("TEST#" + uid));

        System.out.println("POST WRITE FROM REDIS->" + "(" +  uid + ")" + "=" + redisObject.toString());

    }
}