package caselab.service;

import caselab.domain.MyRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class MyService {

    private MyRepository repository;

    public Object get() {
        return new Object();
    }
}
