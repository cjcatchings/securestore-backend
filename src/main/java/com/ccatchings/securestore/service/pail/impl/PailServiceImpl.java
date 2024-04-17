package com.ccatchings.securestore.service.pail.impl;

import com.ccatchings.securestore.database.hibernate.model.Pail;
import com.ccatchings.securestore.database.hibernate.model.PailFile;
import com.ccatchings.securestore.database.hibernate.model.PailFolder;
import com.ccatchings.securestore.database.postgresql.PostgresAdapter;
import com.ccatchings.securestore.service.pail.PailService;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Service("pailService")
public class PailServiceImpl implements PailService {
    @Autowired
    PostgresAdapter dbAdapter;

    public PailServiceImpl(){}

    @Override
    public List<Pail> getPails(Jwt jwt) {
        String preferredUsername = jwt.getClaimAsString("preferred_username");
        List<Pail> pails = dbAdapter.executeQueryWithMultipleResults(genPailQuery(
                Map.of("ownerLogin", preferredUsername)));
        System.out.println(pails);
        return pails;
    }

    public Pail getPail(Jwt jwt, String pailName, Optional<String> fileName){
        String preferredUsername = jwt.getClaimAsString("preferred_username");
        return dbAdapter.executeQueryWithSingleResult(genPailQuery(
                Map.of("ownerLogin", preferredUsername, "name", pailName)));
    }

    public PailFolder getPailFolder(Pail pail, String subFolder, String fileName){
        Map<String, Object> folderQueryMapWithNull = new HashMap<String, Object>();
        folderQueryMapWithNull.put("pail", pail);
        folderQueryMapWithNull.put("parent", null);
        return dbAdapter.executeQueryWithSingleResult(genPailFolderQuery(folderQueryMapWithNull));

    }

    public PailFolder getPailFolder(Pail pail, String[] objPathSplit){
        return dbAdapter.executeQueryWithSingleResult(genPailFolderQuery(Map.of(
                "pail", pail, "path", objPathSplit
        )));
    }

    public List<PailFolder> getPailSubFolders(Pail pail, String subFolder, String fileName){
        Map<String, Object> folderQueryMapWithNull = new HashMap<String, Object>();
        folderQueryMapWithNull.put("pail", pail);
        folderQueryMapWithNull.put("parent", null);
        return dbAdapter.executeQueryWithMultipleResults(genPailFolderQuery(folderQueryMapWithNull));
    }

    public List<PailFolder> getPailSubFolders(Pail pail, PailFolder parentFolder){
        return dbAdapter.executeQueryWithMultipleResults(genSubFolderQuery(pail, parentFolder));
    }

    public PailFile getPailFile(Pail pail, String subFolder, String fileName){
        return dbAdapter.executeQueryWithSingleResult(genPailFileQuery(pail, null, fileName));
    }

    public PailFile getPailFile(Pail pail, PailFolder subFolder, String fileName){
        return dbAdapter.executeQueryWithSingleResult(genPailFileQuery(pail, subFolder, fileName));
    }

    public List<PailFile> getPailFiles(Pail pail){
        return dbAdapter.executeQueryWithMultipleResults(genPailFileQuery(pail, null, null));
    }

    public List<PailFile> getPailFiles(Pail pail, PailFolder parentFolder){
        return dbAdapter.executeQueryWithMultipleResults(genPailFileQuery(pail, parentFolder, null));
    }

    //TODO Move to util class
    private CriteriaQuery<Pail> genPailQuery(Map<String, Object> whereMap){
        CriteriaBuilder builder = dbAdapter.getCriteriaBuilder();
        CriteriaQuery<Pail> critQuery = builder.createQuery(Pail.class);
        Root<Pail> fromModel = critQuery.from(Pail.class);
        critQuery.select(fromModel);
        whereMap.forEach((key, value) -> {
            critQuery.where(builder.equal(fromModel.get(key), value));
        });
        return critQuery;
    }

    private CriteriaQuery<PailFolder> genPailFolderQuery(Map<String, Object> whereMap){
        CriteriaBuilder builder = dbAdapter.getCriteriaBuilder();
        CriteriaQuery<PailFolder> critQuery = builder.createQuery(PailFolder.class);
        Root<PailFolder> fromModel = critQuery.from(PailFolder.class);
        critQuery.select(fromModel);
        List<Predicate> predicateHolder = new ArrayList<Predicate>();
        whereMap.forEach( (key, value) -> {
            if("path".equals(key)){
                if(value == null){
                    predicateHolder.add(builder.isNull(fromModel.get("parent")));
                }else{
                    List<String> pathAsList = Arrays.asList((String[]) value);
                    Collections.reverse(pathAsList);
                    AtomicReference<From<PailFolder, PailFolder>> pailFolderJoin = new AtomicReference<>(null);
                    AtomicInteger currentFolderIx = new AtomicInteger(0);
                    pathAsList.forEach(folder -> {
                        if(pailFolderJoin.get() == null){
                            predicateHolder.add(builder.equal(fromModel.get("folderName"), folder));
                            pailFolderJoin.set(fromModel);
                        }else{
                            From<PailFolder, PailFolder> currentPailFolderJoin = pailFolderJoin.get().join("parent");
                            predicateHolder.add(builder.equal(currentPailFolderJoin.get("folderName"), folder));
                            pailFolderJoin.set(currentPailFolderJoin);
                        }
                        if(currentFolderIx.get() < pathAsList.size() - 1) {
                            currentFolderIx.getAndIncrement();
                        }
                    });
                }
            }else if("parent".equals(key) && value == null){
                predicateHolder.add(builder.isNull(fromModel.get("parent")));
            }else{
                System.out.println("key=" + key + ",val=" + value);
                predicateHolder.add(builder.equal(fromModel.get(key), value));
            }
            Predicate finalAndPredicate = builder.and(predicateHolder.toArray(new Predicate[predicateHolder.size()]));
            critQuery.where(finalAndPredicate);
        });
        return critQuery;
    }

    private CriteriaQuery<PailFolder> genSubFolderQuery(Pail pail, PailFolder parent){
        CriteriaBuilder builder = dbAdapter.getCriteriaBuilder();
        CriteriaQuery<PailFolder> critQuery = builder.createQuery(PailFolder.class);
        Root<PailFolder> fromModel = critQuery.from(PailFolder.class);
        critQuery.select(fromModel);
        Predicate predicate = builder.and(
                builder.equal(fromModel.get("pail"), pail),
                builder.equal(fromModel.get("parent"), parent)
        );
        critQuery.where(predicate);
        return critQuery;
    }

    private CriteriaQuery<PailFile> genPailFileQuery(Pail pail, PailFolder folder, String fileName){
        CriteriaBuilder builder = dbAdapter.getCriteriaBuilder();
        CriteriaQuery<PailFile> critQuery = builder.createQuery(PailFile.class);
        Root<PailFile> fromModel = critQuery.from(PailFile.class);
        critQuery.select(fromModel);
        List<Predicate> predicateList = new ArrayList<Predicate>();
        predicateList.add(folder == null ? builder.isNull(fromModel.get("folder"))
                :builder.equal(fromModel.get("folder"), folder));
        predicateList.add(builder.equal(fromModel.get("pail"), pail));
        if(fileName != null){
            predicateList.add(builder.equal(fromModel.get("fileName"), fileName));
        }
        Predicate mainPredicate = builder.and(
                predicateList.toArray(new Predicate[predicateList.size()])
        );
        critQuery.where(mainPredicate);
        return critQuery;
    }
}
