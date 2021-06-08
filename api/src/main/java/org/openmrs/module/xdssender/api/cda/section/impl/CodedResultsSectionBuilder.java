package org.openmrs.module.xdssender.api.cda.section.impl;

import org.marc.everest.datatypes.BL;
import org.marc.everest.datatypes.II;
import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.datatypes.generic.LIST;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Entry;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section;
import org.marc.everest.rmim.uv.cdar2.vocabulary.x_ActRelationshipEntry;
import org.openmrs.Obs;
import org.openmrs.module.xdssender.XdsSenderConstants;
import org.openmrs.module.xdssender.api.cda.entry.impl.SimpleObservationEntryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Results section builder
 * 
 * @author JustinFyfe
 */
@Component("xdssender.CodedResultsSectionBuilder")
public class CodedResultsSectionBuilder extends SectionBuilderImpl {

	@Autowired
	private SimpleObservationEntryBuilder obsBuilder;
	
	/**
	 * Generate results section
	 */
	@Override
	public Section generate(Entry... entries) {
		Section retVal = super.generate(entries);
		retVal.setTemplateId(LIST.createLIST(new II(XdsSenderConstants.SCT_TEMPLATE_CCD_RESULTS), new II(
		        XdsSenderConstants.SCT_TEMPLATE_CODED_RESULTS)));
		retVal.setTitle("Relevant diagnostic tests/laboratory data");
		retVal.setCode(new CE<String>("30954-2", XdsSenderConstants.CODE_SYSTEM_LOINC,
		        XdsSenderConstants.CODE_SYSTEM_NAME_LOINC, null, "Relevant diagnostic tests/laboratory data", null));
		return retVal;
	}

	/**
	 * Generate coded results section with the specified data
	 */
	public Section generate(Obs obs) {
		Entry codedResults = new Entry(x_ActRelationshipEntry.HasComponent, BL.TRUE, obsBuilder.generate(
				obs));
		return this.generate(codedResults);
	}

	public Section generate (Obs obs1, Obs obs2) {
		Entry codedObs1 = new Entry(x_ActRelationshipEntry.HasComponent, BL.TRUE, obsBuilder.generate(obs1));
		Entry codedObs2 = new Entry(x_ActRelationshipEntry.HasComponent, BL.TRUE, obsBuilder.generate(obs2));
		return this.generate(codedObs1, codedObs2);
	}
}
