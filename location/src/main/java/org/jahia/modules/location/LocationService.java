package org.jahia.modules.location;

import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderRequest;
import com.google.code.geocoder.model.GeocoderResult;
import org.drools.spi.KnowledgeHelper;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.rules.AddedNodeFact;

import javax.jcr.RepositoryException;
import java.util.List;

public class LocationService {

    public void geocodeLocation(AddedNodeFact node, KnowledgeHelper drools) throws RepositoryException {
        final Geocoder geocoder = new Geocoder();
        JCRNodeWrapper nodeWrapper = node.getNode();

        StringBuffer address = new StringBuffer();
        address.append(nodeWrapper.getProperty("j:street").getString());
        if (nodeWrapper.hasProperty("j:zipCode")) {
            address.append(" ").append(nodeWrapper.getProperty("j:zipCode").getString());
        }
        if (nodeWrapper.hasProperty("j:town")) {
            address.append(" ").append(nodeWrapper.getProperty("j:town").getString());
        }
        if (nodeWrapper.hasProperty("j:country")) {
            address.append(" ").append(nodeWrapper.getProperty("j:country").getString());
        }
        if (!nodeWrapper.isNodeType("jnt:location") && !nodeWrapper.isNodeType("jmix:geotagged")) {
            nodeWrapper.addMixin("jmix:geotagged");
        }
        GeocoderRequest geocoderRequest = new GeocoderRequestBuilder().setAddress(address.toString()).getGeocoderRequest();
        GeocodeResponse geocoderResponse = geocoder.geocode(geocoderRequest);
        List<GeocoderResult> results = geocoderResponse.getResults();
        if (results.size() > 0) {
            nodeWrapper.setProperty("j:latitude", results.get(0).getGeometry().getLocation().getLat().toString());
            nodeWrapper.setProperty("j:longitude", results.get(0).getGeometry().getLocation().getLng().toString());
        }
    }
}
