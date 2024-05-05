package com.example.shooterlab2;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;

public class MyDB {
    private final SessionFactory sFactory = HSessionFactory.getSessionFactory();

    public ArrayList<RatingRec> getRatingsTable() {
        ArrayList<RatingRec> res = new ArrayList<>();
        List<RatingRec> list = sFactory.openSession().
                createQuery("FROM RatingRec obj ORDER BY obj.winsCount DESC").list();
        res.addAll(list);
        return res;
    }

    public void insertOrUpdateRec(RatingRec rec) {
        Session session = sFactory.openSession();
        Transaction trn = session.beginTransaction();
        session.saveOrUpdate(rec);
        trn.commit();
        session.close();
    }
}
