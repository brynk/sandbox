package org.ala.biocache.util;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ala.biocache.dao.TaxonConceptDAO;
import org.ala.biocache.dto.OccurrenceDTO;
import org.ala.biocache.dto.SearchQuery;
import org.ala.biocache.model.TaxonConcept;
import org.ala.biocache.web.OccurrenceController;

import org.apache.commons.math.util.MathUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;


import atg.taglib.json.util.JSONObject;
import java.util.Set;
import org.ala.biocache.dto.OccurrenceSourceDTO;
/**
 * A class to provide utility methods used to populate search details.
 * @author Natasha
 */
@Component("searchUtils")
public class SearchUtils {
	
    /** Logger initialisation */
	private final static Logger logger = Logger.getLogger(SearchUtils.class);
    
	@Inject
	protected TaxonConceptDAO taxonConceptDAO;
	
    protected String collectoryBaseUrl = "http://collections.ala.org.au";
    
    protected String bieBaseUrl = "http://bie.ala.org.au";
    
    /**
     * Returns an array that contains the search string to use for a collection
     * search and display name for the results.
     * @param query
     * @return
     */
    public void updateCollectionSearchString(SearchQuery searchQuery) {
        try{
            String query = searchQuery.getQuery();

	        //query the collectory for the institute and collection codes needed to perform the search
	        String jsonObject = OccurrenceController.getUrlContentAsString(collectoryBaseUrl + "/lookup/summary/" + query);
	        JSONObject j = new JSONObject(jsonObject);
	        String collectionName = j.getString("name");
//	        JSONArray institutionCode = j.getJSONArray("derivedInstCodes");
//	        JSONArray collectionCode = j.getJSONArray("derivedCollCodes");
	        StringBuilder displayString = new StringBuilder("Collection: ");
	        displayString.append(collectionName);
//	        // Build Lucene query for institutions
//	        StringBuilder solrQuery = new StringBuilder();
//	        if (institutionCode != null && institutionCode.size() > 0) {
//	
//	            List<String> institutions = new ArrayList<String>();
//	            for (int i = 0; i < institutionCode.size(); i++) {
//	                institutions.add("institution_code:" + institutionCode.getString(i));
//	            }
//	            solrQuery.append("(");
//	            solrQuery.append(StringUtils.join(institutions, " OR "));
//	            solrQuery.append(")");
//	
//	        }
//	
//	        // Build Lucene query for collections
//	        if (collectionCode != null && collectionCode.size() > 0) {
//	            if (solrQuery.length() > 0) {
//	                solrQuery.append(" AND ");
//	
//	            }
//	            //StringBuilder displayString = new StringBuilder("Institution: ");
//	            List<String> collections = new ArrayList<String>();
//	            for (int i = 0; i < collectionCode.size(); i++) {
//	                //quote the collection code to solve issue with invalid characters (eg Invertebrates - Marine & Other)
//	                collections.add("collection_code:\"" + collectionCode.getString(i) +"\"");
//	                //	displayString.append(coll).append(" ");
//	            }
//	            solrQuery.append("(");
//	            solrQuery.append(StringUtils.join(collections, " OR "));
//	            solrQuery.append(")");
//	
//	        }
	        searchQuery.setQuery("collection_code_uid:"+query);
	        searchQuery.setDisplayString(displayString.toString());
	        
        }
        catch(Exception e){
        	logger.error("Problem contacting the collectory: "+e.getMessage(), e);
            //TODO work out what we want to do to the search if an exception occurs while
            //contacting the collectory etc
        }
        
    }
    /**
     * Returns the filter query string required to perform a taxon concept search
     * @param query
     * @return
     */
    public boolean updateTaxonConceptSearchString(SearchQuery searchQuery){
        	
    	String guid = searchQuery.getQuery();
    	TaxonConcept tc = taxonConceptDAO.getByGuid(guid);
    	if(tc!=null){
	        StringBuffer entityQuerySb = new StringBuffer(tc.getRankString() + ": " + tc.getScientificName());
	        if (tc.getCommonName() != null) {
	            entityQuerySb.append(" (");
	            entityQuerySb.append(tc.getCommonName());
	            entityQuerySb.append(") ");
	        }
	        searchQuery.addToFilterQuery("lft:[" + tc.getLeft() + " TO " + tc.getRight() + "]");
	        
	        if (logger.isDebugEnabled()) {
	            for (String filter : searchQuery.getFilterQuery()) {
	                logger.debug("Filter: " + filter);
	            }
	        }
	        searchQuery.setQuery("*:*");
	        searchQuery.setEntityQuery(entityQuerySb.toString());
	        return true;
    	}
    	return false;
    }

    /**
     * Returns the query to be used when searching for data providers.
     * @param query
     * @return
     */

    public String getDataProviderSearchString(String query) {
        return "data_provider_id:" + query;
    }

    /**
     * Returns the query to be used when searching for data resources.
     * @param query
     * @return
     */
    public String getDataResourceSearchString(String query) {
        return "data_resource_id:" + query;
    }
    /**
     * Returns the query string based on the type of search that needs to be performed.
     * @param query
     * @param type
     * @return [0] is the new query string to apply [1] is additional filter query strings to be applied
     */
    public void updateQueryDetails(SearchQuery searchQuery) {
        logger.debug("Processing " + searchQuery.getQuery() +" using type: " + searchQuery.getType());
        if(searchQuery.getType().equals("collection")){
            updateCollectionSearchString(searchQuery);
        }
        else if(searchQuery.getType().equals("provider")){
            searchQuery.setQuery(getDataProviderSearchString(searchQuery.getQuery()));
        }
        else if(searchQuery.getType().equals("resource")){
            searchQuery.setQuery(getDataResourceSearchString(searchQuery.getQuery()));
        }
        else if(searchQuery.getType().equals("taxon")){
            updateTaxonConceptSearchString(searchQuery);
        }
        //otherwise we can leave the query with its default values ("normal" type)
    }
    
    /**
     * Set the initial point values in the index. 
     */
    public static void initialPointValues(OccurrenceDTO occurrence){
    	Double lat = occurrence.getLatitude();
    	Double lon = occurrence.getLongitude();
    	if(lat!=null && lon!=null){
	    	occurrence.setPoint1(MathUtils.round(lat, 0)+","+MathUtils.round(lon, 0));
	    	occurrence.setPoint01(MathUtils.round(lat, 1)+","+MathUtils.round(lon, 1));
	    	occurrence.setPoint001(MathUtils.round(lat, 2)+","+MathUtils.round(lon, 2));
	    	occurrence.setPoint0001(MathUtils.round(lat, 3)+","+MathUtils.round(lon, 3));
	    	occurrence.setPoint00001(MathUtils.round(lat, 4)+","+MathUtils.round(lon, 4));
    	}
    }
    /**
     * returns the solr field that should be used to search for a particular uid
     * @param uid
     * @return
     */
    public static String getUidSearchField(String uid){
        if(uid.startsWith("co"))
            return "collection_code_uid";
        if(uid.startsWith("in"))
            return "institution_code_uid";
        if(uid.startsWith("dr"))
            return "data_resource_uid";
        if(uid.startsWith("dp"))
            return "data_provider_uid";
        return null;
    }
    /**
     * Returns the rank name based on an integer position
     * @param position
     * @return
     */
    public static String getRankFacetName(int position){
        switch(position){
            case 1:return "kingdom";
            case 2:return "phylum";
            case 3:return "class";
            case 4:return "order";
            case 5:return "family";
            case 6:return "genus";
            case 7:return "species";
            default: return "unknown";
        }
    }
	/**
	 * @param taxonConceptDAO the taxonConceptDAO to set
	 */
	public void setTaxonConceptDAO(TaxonConceptDAO taxonConceptDAO) {
		this.taxonConceptDAO = taxonConceptDAO;
	}
	/**
	 * @param collectoryBaseUrl the collectoryBaseUrl to set
	 */
	public void setCollectoryBaseUrl(String collectoryBaseUrl) {
		this.collectoryBaseUrl = collectoryBaseUrl;
	}
	/**
	 * @param bieBaseUrl the bieBaseUrl to set
	 */
	public void setBieBaseUrl(String bieBaseUrl) {
		this.bieBaseUrl = bieBaseUrl;
	}

        /**
         * Returns the information for the supplied source keys
         *
         * TODO: There may be a better location for this method.
         *
         * @param keys
         * @return
         */
        public List<OccurrenceSourceDTO> getSourceInformation(Set<String> keys){
            logger.debug("Listing the source information for : " + keys);
        List<OccurrenceSourceDTO> sources = new ArrayList<OccurrenceSourceDTO>();
        try{
        for(String key : keys){
            //get the information for the uid
            String jsonObject = OccurrenceController.getUrlContentAsString(collectoryBaseUrl + "/lookup/summary/" + key);
	    JSONObject j = new JSONObject(jsonObject);
            sources.add(new OccurrenceSourceDTO(j.getString("name"),key));

        }
        }
        catch(Exception e){
            logger.error(e.getMessage());
        }
        return sources;
    }
}