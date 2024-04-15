package com.ccatchings.securestore.controller.pail;

import com.ccatchings.securestore.database.hibernate.model.Pail;
import com.ccatchings.securestore.database.hibernate.model.PailFile;
import com.ccatchings.securestore.database.postgresql.PostgresAdapter;
import com.ccatchings.securestore.model.PailFileOrFolderList;
import com.ccatchings.securestore.model.PailFileResponse;
import com.ccatchings.securestore.model.PailFolderContentList;
import jakarta.persistence.criteria.*;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import com.ccatchings.securestore.database.hibernate.model.PailFolder;
import org.springframework.web.servlet.HandlerMapping;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@RestController
public class SecureStorePailRestController {

    @Autowired
    PostgresAdapter dbAdapter;

    @GetMapping("/pails")
    public ResponseEntity<List<Pail>> getPails(@AuthenticationPrincipal Jwt jwt){
        String preferredUsername = jwt.getClaimAsString("preferred_username");
        List<Pail> pails = dbAdapter.executeQueryWithMultipleResults(genPailQuery(
                Map.of("ownerLogin", preferredUsername)));
        System.out.println(pails);
        return ResponseEntity.ok(pails);
    }

    @GetMapping("/pails/")
    public ResponseEntity<List<Pail>> getPailsWithTrailingSlash(@AuthenticationPrincipal Jwt jwt){
        return getPails(jwt);
    }

    @GetMapping("/pails/{pailName}")
    public ResponseEntity<PailFileOrFolderList> getPailRootContent(@AuthenticationPrincipal Jwt jwt, @PathVariable String pailName,
                                                                   @RequestParam Optional<String> fileName, HttpServletRequest request){
        return getFolderContentOrFile(jwt, pailName, fileName, request);
    }

    @GetMapping("/pails/{pailName}/")
    public ResponseEntity<PailFileOrFolderList> getPailRootContentTrailingSlash(@AuthenticationPrincipal Jwt jwt, @PathVariable String pailName,
                                                                                @RequestParam Optional<String> fileName, HttpServletRequest request){
        return getFolderContentOrFile(jwt, pailName, fileName, request);
    }

    @GetMapping("/pails/{pailName}/**")
    public ResponseEntity<PailFileOrFolderList> getFolderContentOrFile(@AuthenticationPrincipal Jwt jwt,
                                                                 @PathVariable String pailName, @RequestParam Optional<String> fileName,
                                                                 HttpServletRequest request){
        String preferredUsername = jwt.getClaimAsString("preferred_username");
        Pail pail = dbAdapter.executeQueryWithSingleResult(genPailQuery(
                Map.of("ownerLogin", preferredUsername, "name", pailName)));
        if(pail == null){
            return ResponseEntity.notFound().build();
        }
        PailFolder foundPailFolder;
        Object uriObject = request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String objPath = "";
        if (null != uriObject) {
            objPath = uriObject.toString().replaceFirst("/pails/" + pailName, "");
        }
        List<PailFolder> subFolders;
        List<PailFile> files;
        if("".equals(objPath)){
            if(fileName.isEmpty()) {
                Map<String, Object> folderQueryMapWithNull = new HashMap<String, Object>();
                folderQueryMapWithNull.put("pail", pail);
                folderQueryMapWithNull.put("parent", null);
                subFolders = dbAdapter.executeQueryWithMultipleResults(genPailFolderQuery(folderQueryMapWithNull));
                files = dbAdapter.executeQueryWithMultipleResults(genPailFileQuery(pail, null, null));
                return ResponseEntity.ok(
                        new PailFolderContentList(
                                subFolders.stream().map(pf -> pf.getFolderName()).toList(),
                                files.stream().map(pf -> pf.getFileName()).toList())
                );
            }else{
                //TODO see lines 106-115 and maybe move to function
                PailFile pailFile = dbAdapter.executeQueryWithSingleResult(genPailFileQuery(pail, null, fileName.get()));
                if(pailFile == null){
                    return ResponseEntity.notFound().build();
                }
                //TODO get file content
                return ResponseEntity.ok(
                     new PailFileResponse(pailFile.getMediaType(), pailFile.getFileName(), null)
                );
            }
        }else{
            String[] objPathSplit = objPath.substring(1).split("/");
            foundPailFolder = dbAdapter.executeQueryWithSingleResult(genPailFolderQuery(Map.of(
                    "pail", pail, "path", objPathSplit
            )));
            if(foundPailFolder == null){
                return ResponseEntity.notFound().build();
            }
            if(fileName.isEmpty()) {
                subFolders = dbAdapter.executeQueryWithMultipleResults(genSubFolderQuery(pail, foundPailFolder));
                files = dbAdapter.executeQueryWithMultipleResults(genPailFileQuery(pail, foundPailFolder, null));
                return ResponseEntity.ok(
                        new PailFolderContentList(subFolders.stream().map(
                                pf -> pf.getFolderName()).toList(),
                                files.stream().map(pf -> pf.getFileName()).toList()
                        )
                );
            }else{
                //TODO See lines 84-92 and maybe move to function
                PailFile pailFile = dbAdapter.executeQueryWithSingleResult(genPailFileQuery(pail, foundPailFolder, fileName.get()));
                if(pailFile == null){
                    return ResponseEntity.notFound().build();
                }
                return ResponseEntity.ok(
                        new PailFileResponse(pailFile.getMediaType(), pailFile.getFileName(), null)
                );
            }
        }
    }

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

    //TODO Move to util class
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
