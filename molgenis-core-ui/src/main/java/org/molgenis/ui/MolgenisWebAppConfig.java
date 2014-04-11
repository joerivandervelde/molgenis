package org.molgenis.ui;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.molgenis.data.convert.DateToStringConverter;
import org.molgenis.data.convert.StringToDateConverter;
import org.molgenis.framework.db.WebAppDatabasePopulator;
import org.molgenis.framework.db.WebAppDatabasePopulatorService;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.framework.ui.MolgenisPluginRegistry;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.freemarker.HasPermissionDirective;
import org.molgenis.security.freemarker.NotHasPermissionDirective;
import org.molgenis.ui.freemarker.FormLinkDirective;
import org.molgenis.ui.freemarker.LimitMethod;
import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.util.AsyncJavaMailSender;
import org.molgenis.util.FileStore;
import org.molgenis.util.GsonHttpMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.BufferedImageHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import com.google.common.collect.Maps;

import freemarker.template.TemplateException;

public abstract class MolgenisWebAppConfig extends WebMvcConfigurerAdapter
{
	@Autowired
	private MolgenisSettings molgenisSettings;

	@Autowired
	private MolgenisPermissionService molgenisPermissionService;

	@Autowired
	private WebAppDatabasePopulatorService webAppDatabasePopulatorService;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry)
	{
		registry.addResourceHandler("/css/**").addResourceLocations("/css/", "classpath:/css/").setCachePeriod(3600);
		registry.addResourceHandler("/img/**").addResourceLocations("/img/", "classpath:/img/").setCachePeriod(3600);
		registry.addResourceHandler("/js/**").addResourceLocations("/js/", "classpath:/js/").setCachePeriod(3600);
		registry.addResourceHandler("/generated-doc/**").addResourceLocations("/generated-doc/").setCachePeriod(3600);
		registry.addResourceHandler("/html/**").addResourceLocations("/html/", "classpath:/html/").setCachePeriod(3600);
	}

	@Value("${molgenis.build.profile}")
	private String molgenisBuildProfile;

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters)
	{
		boolean prettyPrinting = molgenisBuildProfile != null && molgenisBuildProfile.equals("dev");
		converters.add(new GsonHttpMessageConverter(prettyPrinting));
		converters.add(new BufferedImageHttpMessageConverter());
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry)
	{
		String pluginInterceptPattern = MolgenisPluginController.PLUGIN_URI_PREFIX + "**";
		registry.addInterceptor(molgenisPluginInterceptor()).addPathPatterns(pluginInterceptPattern);
	}

	@Override
	public void addFormatters(FormatterRegistry registry)
	{
		registry.addConverter(new DateToStringConverter());
		registry.addConverter(new StringToDateConverter());
	}

	@Bean
	public MolgenisPluginInterceptor molgenisPluginInterceptor()
	{
		return new MolgenisPluginInterceptor(molgenisUi());
	}

	@Bean
	public ApplicationListener<?> databasePopulator()
	{
		return new WebAppDatabasePopulator(webAppDatabasePopulatorService);
	}

	@Bean
	public static PropertySourcesPlaceholderConfigurer properties()
	{
		PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
		Resource[] resources = new Resource[]
		{ new FileSystemResource(System.getProperty("molgenis.home") + "/molgenis-server.properties"),
				new ClassPathResource("/molgenis.properties") };
		pspc.setLocations(resources);
		pspc.setFileEncoding("UTF-8");
		pspc.setIgnoreUnresolvablePlaceholders(true);
		pspc.setIgnoreResourceNotFound(true);
		pspc.setNullValue("@null");
		return pspc;
	}

	@Value("${mail.host:smtp.gmail.com}")
	private String mailHost;
	@Value("${mail.port:587}")
	private Integer mailPort;
	@Value("${mail.protocol:smtp}")
	private String mailProtocol;
	@Value("${mail.username}")
	private String mailUsername; // specify in molgenis-server.properties
	@Value("${mail.password}")
	private String mailPassword; // specify in molgenis-server.properties
	@Value("${mail.java.auth:true}")
	private String mailJavaAuth;
	@Value("${mail.java.starttls.enable:true}")
	private String mailJavaStartTlsEnable;
	@Value("${mail.java.quitwait:false}")
	private String mailJavaQuitWait;

	@Bean
	public JavaMailSender mailSender()
	{
		AsyncJavaMailSender mailSender = new AsyncJavaMailSender();
		mailSender.setHost(mailHost);
		mailSender.setPort(mailPort);
		mailSender.setProtocol(mailProtocol);
		mailSender.setUsername(mailUsername); // specify in molgenis-server.properties
		mailSender.setPassword(mailPassword); // specify in molgenis-server.properties
		Properties javaMailProperties = new Properties();
		javaMailProperties.setProperty("mail.smtp.auth", mailJavaAuth);
		javaMailProperties.setProperty("mail.smtp.starttls.enable", mailJavaStartTlsEnable);
		javaMailProperties.setProperty("mail.smtp.quitwait", mailJavaQuitWait);
		mailSender.setJavaMailProperties(javaMailProperties);
		return mailSender;
	}

	@Bean
	public FileStore fileStore()
	{
		// get molgenis home directory
		String molgenisHomeDir = System.getProperty("molgenis.home");
		if (molgenisHomeDir == null)
		{
			throw new IllegalArgumentException("missing required java system property 'molgenis.home'");
		}
		if (!molgenisHomeDir.endsWith("/")) molgenisHomeDir = molgenisHomeDir + '/';

		// create molgenis store directory in molgenis data directory if not exists
		String molgenisFileStoreDirStr = molgenisHomeDir + "data/filestore";
		File molgenisDataDir = new File(molgenisFileStoreDirStr);
		if (!molgenisDataDir.exists())
		{
			if (!molgenisDataDir.mkdir())
			{
				throw new RuntimeException("failed to create directory: " + molgenisFileStoreDirStr);
			}
		}

		return new FileStore(molgenisFileStoreDirStr);
	}

	/**
	 * Bean that allows referencing Spring managed beans from Java code which is not managed by Spring
	 * 
	 * @return
	 */
	@Bean
	public ApplicationContextProvider applicationContextProvider()
	{
		return new ApplicationContextProvider();
	}

	/**
	 * Enable spring freemarker viewresolver. All freemarker template names should end with '.ftl'
	 */
	@Bean
	public ViewResolver viewResolver()
	{
		FreeMarkerViewResolver resolver = new FreeMarkerViewResolver();
		resolver.setCache(true);
		resolver.setSuffix(".ftl");

		return resolver;
	}

	/**
	 * Configure freemarker. All freemarker templates should be on the classpath in a package called 'freemarker'
	 * 
	 * @throws TemplateException
	 * @throws IOException
	 */
	@Bean
	public FreeMarkerConfigurer freeMarkerConfigurer() throws IOException, TemplateException
	{
		FreeMarkerConfigurer result = new FreeMarkerConfigurer();
		result.setPreferFileSystemAccess(false);
		result.setTemplateLoaderPath("classpath:/templates/");
		result.setDefaultEncoding("UTF-8");

		Map<String, Object> freemarkerVariables = Maps.newHashMap();
		freemarkerVariables.put("limit", new LimitMethod());
		freemarkerVariables.put("hasPermission", new HasPermissionDirective(molgenisPermissionService));
		freemarkerVariables.put("notHasPermission", new NotHasPermissionDirective(molgenisPermissionService));
		freemarkerVariables.put("formLink", new FormLinkDirective());
		addFreemarkerVariables(freemarkerVariables);

		result.setFreemarkerVariables(freemarkerVariables);

		return result;
	}

	// Override in subclass if you need more freemarker variables
	protected void addFreemarkerVariables(Map<String, Object> freemarkerVariables)
	{

	}

	@Bean
	public MultipartResolver multipartResolver()
	{
		return new StandardServletMultipartResolver();
	}

	@Bean
	public MolgenisUi molgenisUi()
	{
		try
		{
			return new XmlMolgenisUi(new XmlMolgenisUiLoader(), molgenisSettings, molgenisPermissionService);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Bean
	public MolgenisPluginRegistry molgenisPluginRegistry()
	{
		return new MolgenisUiPluginRegistry(molgenisUi());
	}
}
