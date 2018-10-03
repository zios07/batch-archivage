package com.cirb.archiver.batch.jobs;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import com.cirb.archiver.batch.tasklets.ArchivingTasklet;
import com.cirb.archiver.batch.tasklets.EncryptionTasklet;
import com.cirb.archiver.batch.tasklets.SolrTasklet;
import com.cirb.archiver.batch.utils.ArchiveJsonItemAggregator;
import com.cirb.archiver.domain.JsonArchive;
import com.cirb.archiver.repositories.ConsumerRepository;
import com.cirb.archiver.repositories.ProviderRepository;
import com.cirb.archiver.repositories.SolrArchiveRepository;

@Configuration
public class ArchivingJob {

	protected final Log logger = LogFactory.getLog(getClass());

	@Value("${batch.archive-directory}")
	private String archiveDirectory;

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Autowired
	private ConsumerRepository consumerRepository;

	@Autowired
	private ProviderRepository providerRepository;
	
	@Autowired
	private SolrArchiveRepository solrArchiveRepository;

	@Bean
	public Job archiverJob() {
		return jobBuilderFactory.get("archivingJob")
				.incrementer(new RunIdIncrementer())
				.start(archivingStep())
				.next(solrStep())
				.build();
	}

	// Steps config

	@Bean
	protected Step archivingStep() {
		return stepBuilderFactory.get("archivingStep").tasklet(archivingTasklet()).build();
	}

	@Bean
	protected Step encryptingStep() {
		return stepBuilderFactory.get("encryptingStep").tasklet(encryptionTasklet()).build();
	}

	@Bean
	protected Step solrStep() {
		return stepBuilderFactory.get("solrStep").tasklet(solrTasklet(solrArchiveRepository, archiveDirectory)).build();
	}

	// Tasklets config

	@Bean
	@StepScope
	protected Tasklet archivingTasklet() {
		return new ArchivingTasklet(consumerRepository, providerRepository, writer());
	}

	@Bean
	protected Tasklet encryptionTasklet() {
		return new EncryptionTasklet(archiveDirectory);
	}
	
	@Bean
	protected Tasklet solrTasklet(SolrArchiveRepository solrArchiveRepository, String path) {
		return new SolrTasklet(solrArchiveRepository, path);
	}

	// ItemWriter config

	@Bean
	@StepScope
	public FlatFileItemWriter<JsonArchive> writer() {
		FlatFileItemWriter<JsonArchive> writer = new FlatFileItemWriter<>();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		String date = sdf.format(new Date());
		writer.setResource(new FileSystemResource(archiveDirectory + "archive_" + date + ".json"));
		writer.setSaveState(true);
		writer.open(new ExecutionContext());
		writer.setLineAggregator(new ArchiveJsonItemAggregator());
		return writer;
	}

}
