package com.ccatchings.securestore.bootstrap;

import com.ccatchings.securestore.database.hibernate.model.PailFile;
import com.ccatchings.securestore.database.hibernate.model.PailFolder;
import com.ccatchings.securestore.database.hibernate.model.Pail;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.logging.LoggerFactory;
import org.keycloak.test.TestsHelper;

import java.util.List;
import org.junit.platform.commons.logging.Logger;

import static org.keycloak.test.TestsHelper.deleteRealm;
import static org.keycloak.test.TestsHelper.importTestRealm;

public class BeforeAllExtension implements BeforeAllCallback, ExtensionContext.Store.CloseableResource{
    private static SessionFactory sessionFactory;
    private static Session session;
    private static String origKeycloakTestUrl = TestsHelper.keycloakBaseUrl;
    private static boolean started = false;
    private static Logger logger = LoggerFactory.getLogger(BeforeAllExtension.class);

    private static List<Pail> testPails = List.of(
            new Pail("firstPail", "secstoreuser"),
            new Pail("Taxes", "ccatchings")
    );

    private static List<PailFolder> testRootPailFolders = List.of(
            new PailFolder(testPails.get(0), "teams", null),
            new PailFolder(testPails.get(1), "years", null)
    );

    private static List<PailFolder> testPailSubFolders = List.of(
            new PailFolder(testPails.get(0), "ATL", testRootPailFolders.get(0)),
            new PailFolder(testPails.get(0), "BOS", testRootPailFolders.get(0)),
            new PailFolder(testPails.get(0), "ORL", testRootPailFolders.get(0))
    );

    private static List<PailFile> testPailFiles = List.of(
            new PailFile("BBogdanovic.json", testPails.get(0), testPailSubFolders.get(0), "application/json"),
            new PailFile("TYoung.json", testPails.get(0), testPailSubFolders.get(0), "application/json"),
            new PailFile("JTatum.json", testPails.get(0), testPailSubFolders.get(1), "application/json"),
            new PailFile("FWagner.json", testPails.get(0), testPailSubFolders.get(2), "application/json")
    );
    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        if(started){
            logger.info(() -> "BeforeAll already executed");
            return;
        }
        logger.info(() -> "BeforeAll now executing");
        try {
            TestsHelper.keycloakBaseUrl = "http://localhost:8280";
            importTestRealm("admin", "admin", "/keycloak/realms/securestorerealm.json");
        } catch (Exception e) {
            e.printStackTrace();
        }
        sessionFactory = new Configuration().configure().buildSessionFactory();
        session = sessionFactory.openSession();
        Session finalSession = session;
        finalSession.beginTransaction();
        testPails.forEach(p -> finalSession.persist(p));
        testRootPailFolders.forEach(rf -> finalSession.persist(rf));
        testPailSubFolders.forEach(sf -> finalSession.persist(sf));
        testPailFiles.forEach(f -> finalSession.persist(f));
        finalSession.getTransaction().commit();
        started = true;
    }

    @Override
    public void close() throws Throwable {
        deleteRealm("admin", "admin", "securestoretest");
        TestsHelper.keycloakBaseUrl = origKeycloakTestUrl;
        if(session != null){
            session.close();
        }
        if(sessionFactory != null){
            sessionFactory.close();
        }
    }
}
