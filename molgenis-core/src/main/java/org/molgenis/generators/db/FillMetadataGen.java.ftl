<#include "GeneratorHelper.ftl">
package ${package};

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

<#if metaData>
import org.molgenis.omx.auth.MolgenisGroup;
import org.molgenis.omx.auth.MolgenisPermission;
import org.molgenis.omx.auth.MolgenisRoleGroupLink;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.core.EntityClass;
import org.molgenis.omx.core.SystemClass;
</#if>

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import java.text.ParseException;
<#if databaseImpl == 'JPA'>
import javax.persistence.EntityManager;
</#if>
import org.molgenis.framework.security.Login;
import org.molgenis.framework.security.SimpleLogin;

public class FillMetadata {
	protected static final Logger logger = Logger.getLogger(FillMetadata.class);
<#if !metaData>
	public static void fillMetadata(Database db) throws Exception {
		logger.info("fillMetadata is Empty!");
	}
	public static void fillMetadata(Database db, boolean useLogin) throws Exception {
		logger.info("fillMetadata is Empty!");
	}
<#else>
	public static void fillMetadata(Database db) throws Exception {
		fillMetadata(db, true);
	}
	
	public static void fillMetadata(Database db, boolean useLogin) throws Exception{
		fillMetadata(db, useLogin, "UserLoginPlugin");
	}
	
	public static void fillMetadata(Database db, boolean useLogin, String loginPluginName) throws Exception {
        System.out.println("fillMetadata start");

		Login login = db.getLogin();
        if(useLogin)
        {
            if(login == null) {
                System.out.println("login == null --> no meta data added");           
                return;
            } else if (login instanceof SimpleLogin) {
            	System.out.println("login instanceof SimpleLogin --> no meta data added");
            	return;
            }
        } else {
    		db.setLogin(null); // so we don't run into trouble with the Security Decorators
        }


<#if databaseImpl == 'JPA'>
            EntityManager em = db.getEntityManager();
            em.getTransaction().begin();
</#if>


		MolgenisUser user1 = new MolgenisUser();
		user1.setName("admin");
		user1.setIdentifier("admin");
		user1.setPassword("md5_21232f297a57a5a743894a0e4a801fc3");
		user1.setEmail("");
		user1.setFirstName("admin");
		user1.setLastName("admin");
		user1.setActive(true);
		user1.setSuperuser(true);
		
		MolgenisUser user2 = new MolgenisUser();
		user2.setName("anonymous");
		user2.setIdentifier("anonymous");
		user2.setPassword("md5_294de3557d9d00b3d2d8a1e6aab028cf");
		user2.setEmail("");
		user2.setFirstName("anonymous");
		user2.setLastName("anonymous");
		user2.setActive(true);

		MolgenisGroup group1 = new MolgenisGroup();
		group1.setName("system");
		group1.setIdentifier("system");
		MolgenisGroup group2 = new MolgenisGroup();
		group2.setName("AllUsers");
		group2.setIdentifier("AllUsers");

<#if databaseImpl == 'JPA'>
        em.persist(user1);
        em.persist(user2);
        em.persist(group1);
        em.persist(group2);

        em.getTransaction().commit();
     
        login.login(db, "admin", "admin");

        db.beginTx();
<#else>
        db.beginTx();
		//doesn't work fix:
		db.add(user1);
		db.add(user2);
		db.add(group1);
		db.add(group2);	
</#if>


<#list model.getUserinterface().getAllUniqueGroups() as group>
		{
			MolgenisGroup group = new MolgenisGroup();
			group.setName("${group}");
			db.add(group);
		}
</#list>

		MolgenisRoleGroupLink mrgl1 = new MolgenisRoleGroupLink();
		mrgl1.setGroup_Id(group1.getId());
//		mrgl1.setIdentifier(group1.getIdentifier());
//		mrgl1.setName(group1.getName());
		mrgl1.setRole(user1.getId());

		MolgenisRoleGroupLink mrgl2 = new MolgenisRoleGroupLink();
		mrgl2.setGroup_Id(group2.getId());
//		mrgl2.setIdentifier(group2.getIdentifier());
//		mrgl2.setName(group2.getName());
		mrgl2.setRole(user1.getId());		

		MolgenisRoleGroupLink mrgl3 = new MolgenisRoleGroupLink();
		mrgl3.setGroup_Id(group1.getId());
//		mrgl3.setIdentifier("mrgl3");
//		mrgl3.setName(group1.getName());
		mrgl3.setRole(user2.getId());

		MolgenisRoleGroupLink mrgl4 = new MolgenisRoleGroupLink();
		mrgl4.setGroup_Id(group2.getId());
//		mrgl4.setIdentifier("mrgl4");
//		mrgl4.setName(group2.getName());
		mrgl4.setRole(user2.getId());		
		
		db.add(mrgl1);
		db.add(mrgl2);
		db.add(mrgl3);
		db.add(mrgl4);
		
		{
			List<EntityClass> entites = createEntities(ENTITY_VALUES);
			db.add(entites);
		}
		{
			List<SystemClass> entites = createUIEntities(UI_VALUES);
			db.add(entites);
		}

<#assign schema = model.getUserinterface()>		
<#list schema.getAllChildren() as screen>
	<#if screen.getGroup()?exists || screen.getGroupRead()?exists>
		<#if screen.getType() == "FORM">
		{
			MolgenisGroup role = MolgenisGroup.findByName(db, "<#if screen.getGroup()?exists>${screen.getGroup()}<#else>${screen.getGroupRead()}</#if>");	
			SystemClass entity = db.find(SystemClass.class, new QueryRule(SystemClass.ENTITYNAME, Operator.EQUALS, "${screen.getName()}${screen.getType()?lower_case?cap_first}Controller")).get(0);
			
			MolgenisPermission mp = new MolgenisPermission();
			mp.setRole(role.getId());
			mp.setName(role.getName());
			mp.setIdentifier(role.getIdentifier());
			mp.setEntity(entity.getId());
			mp.setPermission("<#if screen.getGroup()?exists>write<#else>read</#if>");
			db.add(mp);
		}		
		{
			SystemClass id = db.find(SystemClass.class, new QueryRule(SystemClass.ENTITYCLASSNAME, Operator.EQUALS, "${screen.getEntity().namespace}.${screen.getEntity().name}")).get(0);
			MolgenisGroup role = MolgenisGroup.findByName(db, "<#if screen.getGroup()?exists>${screen.getGroup()}<#else>${screen.getGroupRead()}</#if>");
			SystemClass entity = db.find(SystemClass.class, new QueryRule(SystemClass.ID, Operator.EQUALS, id.getId())).get(0);
			
			MolgenisPermission mp = new MolgenisPermission();
			mp.setRole(role.getId());
			mp.setName(role.getName());
			mp.setIdentifier(role.getIdentifier());
			mp.setEntity(entity.getId());
			mp.setPermission("<#if screen.getGroup()?exists>write<#else>read</#if>");
			db.add(mp);
		}
		<#else>
		{
			MolgenisGroup role = MolgenisGroup.findByName(db,"<#if screen.getGroup()?exists>${screen.getGroup()}<#else>${screen.getGroupRead()}</#if>");		
			SystemClass entity = db.find(SystemClass.class, new QueryRule(SystemClass.ENTITYNAME, Operator.EQUALS, "${screen.getName()}${screen.getType()?lower_case?cap_first}")).get(0);
			
			MolgenisPermission mp = new MolgenisPermission();
			mp.setRole(role.getId());
			mp.setName(role.getName());
			mp.setIdentifier(role.getIdentifier());
			mp.setEntity(entity.getId());
			mp.setPermission("<#if screen.getGroup()?exists>write<#else>read</#if>");
			db.add(mp);
		}			
		</#if>
	</#if>
</#list>
		{
			//INSERT INTO MolgenisPermission (role_, entity, permission) SELECT 3, id, 'read' FROM MolgenisEntity WHERE MolgenisEntity.name = 'UserLoginPlugin';
			SystemClass insertEntities = db.find(SystemClass.class, new QueryRule(SystemClass.ENTITYNAME, Operator.EQUALS, loginPluginName)).get(0);		
			MolgenisPermission mp = new MolgenisPermission();
			mp.setRole(user2.getId());
			mp.setName(user2.getName());
			mp.setIdentifier(user2.getIdentifier());
			mp.setEntity(insertEntities.getId());
			mp.setPermission("read");
			db.add(mp);
		}
		
		db.commitTx();
		
		db.setLogin(login); // restore login
		
		logger.info("fillMetadata end");
	}
	
	public static List<EntityClass> createEntities(String[][] entityValues) {
		List<EntityClass> result = new ArrayList<EntityClass>(entityValues.length);
		for(String[] values : entityValues) {
			EntityClass entity = new EntityClass();
			entity.setEntityName(values[0]);
			entity.setEntityType(values[1]);
			entity.setEntityClassName(values[2]);
			result.add(entity);      
		}		
		return result;		
	}
	
	public static List<SystemClass> createUIEntities(String[][] entityValues) {
		List<SystemClass> result = new ArrayList<SystemClass>(entityValues.length);
		for(String[] values : entityValues) {
			SystemClass entity = new SystemClass();
			entity.setEntityName(values[0]);
			entity.setEntityType(values[1]);
			entity.setEntityClassName(values[2]);
			result.add(entity);      
		}		
		return result;		
	}
	
	private static final String[][] ENTITY_VALUES = new String[][] {
<#list model.getConcreteEntities() as entity>
		new String[] {"${JavaName(entity)}", "ENTITY", "${entity.namespace}.${JavaName(entity)}"}<#if entity_has_next>,</#if>
</#list>
	};

	private static final String[][] UI_VALUES = new String[][] {
<#assign schema = model.getUserinterface()>
<#list schema.getAllChildren() as screen>
	<#if screen.getType() == "FORM">
		new String[] {"${screen.getName()}${screen.getType()?lower_case?cap_first}Controller", "${screen.getType()}", "app.ui.${screen.getName()}${screen.getType()?lower_case?cap_first}Controller"}<#if screen_has_next>,</#if>
	<#else>
		new String[] {"${screen.getName()}${screen.getType()?lower_case?cap_first}", "${screen.getType()}", "app.ui.${screen.getName()}${screen.getType()?lower_case?cap_first}"}<#if screen_has_next>,</#if>
	</#if>
</#list>	
	}; 
</#if>
}
