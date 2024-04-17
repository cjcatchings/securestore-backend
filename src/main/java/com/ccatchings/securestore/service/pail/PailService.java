package com.ccatchings.securestore.service.pail;

import com.ccatchings.securestore.database.hibernate.model.Pail;
import com.ccatchings.securestore.database.hibernate.model.PailFile;
import com.ccatchings.securestore.database.hibernate.model.PailFolder;
import com.ccatchings.securestore.database.postgresql.PostgresAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

public interface PailService {

    public List<Pail> getPails(Jwt jwt);

    public Pail getPail(Jwt jwt, String pailName, Optional<String> fileName);

    public PailFolder getPailFolder(Pail pail, String subFolder, String fileName);

    public PailFolder getPailFolder(Pail pail, String[] objPathSplit);

    public List<PailFolder> getPailSubFolders(Pail pail, String subFolder, String fileName);

    public List<PailFolder> getPailSubFolders(Pail pail, PailFolder parentFolder);

    public PailFile getPailFile(Pail pail, String subFolder, String fileName);

    public PailFile getPailFile(Pail pail, PailFolder subFolder, String fileName);

    public List<PailFile> getPailFiles(Pail pail);

    public List<PailFile> getPailFiles(Pail pail, PailFolder parentFolder);
}
