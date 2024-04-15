package com.ccatchings.securestore.model;

import java.util.List;

public record PailFileResponse(
        String contentType,
        String fileName,
        byte[] content) implements PailFileOrFolderList {
}
