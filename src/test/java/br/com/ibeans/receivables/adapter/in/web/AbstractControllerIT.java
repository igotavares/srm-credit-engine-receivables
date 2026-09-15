package br.com.ibeans.receivables.adapter.in.web;

import br.com.ibeans.receivables.AbstractIT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
public abstract class AbstractControllerIT extends AbstractIT {

    @Autowired
    protected MockMvc mockMvc;

}
