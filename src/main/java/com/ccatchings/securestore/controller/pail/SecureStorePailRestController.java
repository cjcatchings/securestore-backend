package com.ccatchings.securestore.controller.pail;

import com.ccatchings.securestore.database.hibernate.model.Pail;
import com.ccatchings.securestore.database.hibernate.model.PailFile;
import com.ccatchings.securestore.database.postgresql.PostgresAdapter;
import com.ccatchings.securestore.model.PailFileOrFolderList;
import com.ccatchings.securestore.model.PailFileResponse;
import com.ccatchings.securestore.model.PailFolderContentList;
import com.ccatchings.securestore.service.pail.PailService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

@RestController
public class SecureStorePailRestController {

    @Autowired
    @Qualifier("pailService")
    PailService pailService;

    @GetMapping("/pails")
    public ResponseEntity<List<Pail>> getPails(@AuthenticationPrincipal Jwt jwt){
        return ResponseEntity.ok(pailService.getPails(jwt));
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
        Pail pail = pailService.getPail(jwt, pailName, fileName);
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
                subFolders = pailService.getPailSubFolders(pail, null, null);
                files = pailService.getPailFiles(pail);
                return ResponseEntity.ok(
                        new PailFolderContentList(
                                subFolders.stream().map(pf -> pf.getFolderName()).toList(),
                                files.stream().map(pf -> pf.getFileName()).toList())
                );
            }else{
                //TODO see lines 106-115 and maybe move to function
                PailFile pailFile = pailService.getPailFile(pail,  (String) null, fileName.get());
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
            foundPailFolder = pailService.getPailFolder(pail, objPathSplit);
            if(foundPailFolder == null){
                return ResponseEntity.notFound().build();
            }
            if(fileName.isEmpty()) {
                subFolders = pailService.getPailSubFolders(pail, foundPailFolder);
                files = pailService.getPailFiles(pail, foundPailFolder);
                return ResponseEntity.ok(
                        new PailFolderContentList(subFolders.stream().map(
                                pf -> pf.getFolderName()).toList(),
                                files.stream().map(pf -> pf.getFileName()).toList()
                        )
                );
            }else{
                //TODO See lines 84-92 and maybe move to function
                PailFile pailFile = pailService.getPailFile(pail, foundPailFolder, fileName.get());
                if(pailFile == null){
                    return ResponseEntity.notFound().build();
                }
                return ResponseEntity.ok(
                        new PailFileResponse(pailFile.getMediaType(), pailFile.getFileName(), null)
                );
            }
        }
    }

}
