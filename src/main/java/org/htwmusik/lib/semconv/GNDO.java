package org.htwmusik.lib.semconv;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;

public class GNDO {
	public static final String uri ="http://d-nb.info/standards/elementset/gnd#";

    /** returns the URI for this schema
     * @return the URI for this schema
     */
    public static String getURI() {
          return uri;
    }
    
    static class SC {
    	public static final String uri ="http://d-nb.info/standards/vocab/gnd/gnd-sc#";

        /** returns the URI for this schema
         * @return the URI for this schema
         */
        static String getURI() {
              return uri;
        }
        public static final Property MUSIC_PERSON = m.createProperty(uri + "14.4p" );
        
    }
    
    private static final Model m = ModelFactory.createDefaultModel();
    public static final Property FORENAME = m.createProperty(uri + "forename" );
    public static final Property SURNAME = m.createProperty(uri + "surname" );
    public static final Property GND_SUBJECT_CATEGORY = m.createProperty(uri + "gndSubjectCategory" );
    public static final Property MUSICAL_WORK = m.createProperty(uri + "MusicalWork" );
    public static final Property PREFERRED_NAME_FOR_THE_WORK = m.createProperty(uri + "preferredNameForTheWork" );
    public static final Property FIRST_COMPOSER = m.createProperty(uri + "firstComposer" );
    public static final Property preferredNameEntityForThePerson = m.createProperty(uri + "preferredNameEntityForThePerson" );
    
//    public static final Property FIRST_COMPOSER = m.createProperty(uri + "firstComposer" );
//    public static final Property FIRST_COMPOSER = m.createProperty(uri + "firstComposer" );
    
    
}
