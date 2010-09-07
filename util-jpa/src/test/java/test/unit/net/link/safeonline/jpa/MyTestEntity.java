/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.jpa;

import static test.unit.net.link.safeonline.jpa.MyTestEntity.*;

import java.io.Serializable;
import java.util.List;
import javax.persistence.*;
import net.link.safeonline.jpa.annotation.QueryMethod;
import net.link.safeonline.jpa.annotation.QueryParam;
import net.link.safeonline.jpa.annotation.UpdateMethod;


@Entity
@NamedQueries({
        @NamedQuery(name = QUERY_ALL, query = "FROM MyTestEntity"),
        @NamedQuery(name = QUERY_WHERE_NAME, query = "FROM MyTestEntity AS mte WHERE mte.name = :" + NAME_PARAM),
        @NamedQuery(name = DELETE_ALL, query = "DELETE FROM MyTestEntity"),
        @NamedQuery(name = COUNT_ALL, query = "SELECT COUNT(*) FROM MyTestEntity") })
public class MyTestEntity implements Serializable {

    public static final String QUERY_ALL = "mte.all";

    public static final String QUERY_WHERE_NAME = "mte.name";

    public static final String DELETE_ALL = "mte.del.all";

    public static final String NAME_PARAM = "name";

    public static final String COUNT_ALL = "count.all";

    public MyTestEntity() {

        this( null );
    }

    public MyTestEntity(String name) {

        this.name = name;
    }

    private String name;

    @Id
    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public interface MyQueryTestInterface {

        @QueryMethod(QUERY_ALL)
        List<MyTestEntity> listAll();

        @QueryMethod(QUERY_WHERE_NAME)
        List<MyTestEntity> listAll(@QueryParam(NAME_PARAM) String name);

        @QueryMethod(QUERY_WHERE_NAME)
        MyTestEntity get(@QueryParam(NAME_PARAM) String name);

        @QueryMethod(value = QUERY_WHERE_NAME, nullable = true)
        MyTestEntity find(@QueryParam(NAME_PARAM) String name);

        @UpdateMethod(DELETE_ALL)
        void removeAll();

        @UpdateMethod(DELETE_ALL)
        int removeAllReturningInt();

        @QueryMethod(COUNT_ALL)
        long countAll();

        @UpdateMethod(DELETE_ALL)
        Integer removeAllReturningInteger();

        @QueryMethod(QUERY_ALL)
        Query listAllQuery();
    }
}
