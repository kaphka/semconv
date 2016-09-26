package org.htwmusik.lib.semconv;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.function.Predicate;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

public class GndFilter {

	private static final Logger LOG = LoggerFactory.getLogger(GndFilter.class);

	public static void main(String[] args) throws IOException {
		// TODO: check arguments first
		Properties exportProb = loadProperties("/default.properties");
		new GndFilter().load(exportProb.getProperty("gnbdb"), exportProb.getProperty("exportdir"));
	}

	private static Properties loadProperties(String probFilePath) {
		Properties prob = new Properties();
		try (InputStream is = GndFilter.class.getResourceAsStream(probFilePath)) {
			prob.load(is);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (LOG.isDebugEnabled()) {
			prob.list(System.out);
		}
		return prob;
	}

	class GndItem {
		@JsonIgnore
		public final String id;

		public GndItem(Resource ressource) {
			this.id = ressource.getURI();
		}
	}

	class MusicalWork extends GndItem {
		public final String name;

		public MusicalWork(Resource ressource) {
			super(ressource);
			this.name = ressource.getProperty(GNDO.PREFERRED_NAME_FOR_THE_WORK).getObject().asLiteral().getString();

		}
	}

	class Person extends GndItem {
		public final String surname;
		public final String forname;

		public Person(Resource resource) {
			super(resource);
			this.forname = resource.getProperty(GNDO.FORENAME).getObject().asLiteral().getString();
			this.surname = resource.getProperty(GNDO.SURNAME).getObject().asLiteral().getString();
		}
	}

	class CsvExporter {

		private final Path dir;

		public CsvExporter(Path path) {
			this.dir = path;
		}

		public SequenceWriter createCsvWriter(Class<?> pojoType) throws IOException {
			File file = dir.resolve(pojoType.getSimpleName() + ".csv").toFile();
			CsvMapper mapper = new CsvMapper();
			CsvSchema schema = mapper.schemaFor(pojoType).withHeader().withEscapeChar('\\');
			ObjectWriter myObjectWriter = mapper.writer(schema);
			FileOutputStream tempFileOutputStream = new FileOutputStream(file);
			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(tempFileOutputStream, 1024);
			OutputStreamWriter writerOutputStream = new OutputStreamWriter(bufferedOutputStream, "UTF-8");
			SequenceWriter swriter = myObjectWriter.writeValues(writerOutputStream);
			return swriter;
		}
	}

	private void load(String dataSetPath, String exportDir) throws IOException {
		CsvExporter exporter = new CsvExporter(Paths.get(exportDir));
		SequenceWriter mwork_writer = exporter.createCsvWriter(MusicalWork.class);
		SequenceWriter person_writer = exporter.createCsvWriter(Person.class);

		LOG.info("Loading database: " + dataSetPath);
		Dataset dataset = TDBFactory.createDataset(dataSetPath);
		dataset.begin(ReadWrite.READ);

		Model model = dataset.getDefaultModel();
		

		Predicate<Resource> hasPropertiers = ressource -> {
			return ressource.hasProperty(GNDO.PREFERRED_NAME_FOR_THE_WORK)
					&& ressource.hasProperty(GNDO.FIRST_COMPOSER);
		};
		Predicate<Resource> hasNameEntity = ressource -> {
			return ressource.hasProperty(GNDO.preferredNameEntityForThePerson);
		};
		Predicate<Resource> hasFullName = ressource -> {
			return ressource.hasProperty(GNDO.SURNAME) && ressource.hasProperty(GNDO.FORENAME);
		};
		ResIterator work_iter = model.listResourcesWithProperty(null, GNDO.MUSICAL_WORK);
		ExtendedIterator<Resource> authors = work_iter.filterKeep(hasPropertiers).mapWith(ressource -> {
			try {
				mwork_writer.write(new MusicalWork(ressource));
			} catch (Exception e) {
				LOG.warn(e.getMessage());
			}
			Statement stmnt = ressource.getProperty(GNDO.FIRST_COMPOSER);
			return stmnt.getObject().asResource();
		}).filterKeep(hasNameEntity).mapWith(ressource -> {
			Statement stmnt = ressource.getProperty(GNDO.preferredNameEntityForThePerson);
			return stmnt.getObject().asResource();
		}).filterKeep(hasFullName).mapWith(resource -> {
			try {
				person_writer.write(new Person(resource));
			} catch (Exception e) {
				LOG.warn(e.getMessage());
			}
			return resource;
		});

		while (authors.hasNext()) {
			Resource stmnt = (Resource) authors.next();
		}

		LOG.info("Closing");
		dataset.end();
		dataset.close();
		LOG.info("Closed");
	}
}
