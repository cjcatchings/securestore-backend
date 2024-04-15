package com.ccatchings.securestore.model;

import java.util.List;

public record PailFolderContentList(
        List<String> subFolders,
        List<String> files) implements PailFileOrFolderList{
}
