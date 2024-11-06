package caselab.domain.storage;

import java.io.InputStream;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorage {

    String put(MultipartFile obj);

    InputStream get(String url);

    boolean delete(String url);

}
