package com.wellogic.appointments;

import com.wellogic.appointments.web.TestWebAppConfig;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

/**
 * The base class for REST/MVC controller test implementations.
 *
 * @author smitha
 */
@ActiveProfiles({ "test", "springDataJpa" })
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestWebAppConfig.class)
@AutoConfigureDataJpa
@AutoConfigureTestDatabase
@Ignore
public abstract class WebApplicationTest
{
    @Autowired
    protected MockMvc mvc;

}
