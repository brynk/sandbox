/***************************************************************************
 * Copyright (C) 2010 Atlas of Living Australia
 * All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 ***************************************************************************/
package au.org.ala.sensitiveData.dao;

import java.util.List;

import au.org.ala.sensitiveData.model.RawOccurrenceRecord;

/**
 *
 * @author Peter Flemming (peter.flemming@csiro.au)
 */
public interface RawOccurrenceDao {

	List<RawOccurrenceRecord> getOccurrences();
	
	void updateLocation(int id, String rawLatitude, String rawLongitude, int generalisedMetres, String generalisedLatitude, String generalisedLongitude);
	
	void updateLocation(int id, String generalisedLatitude, String generalisedLongitude, int generalisedMetres);
}