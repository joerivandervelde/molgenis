package org.molgenis.lifelines.hl7;

import org.molgenis.hl7.CD;
import org.molgenis.hl7.REPCMT000100UV01Organizer;
import org.molgenis.omx.core.Protocol;

public class HL7OrganizerConvertor
{
	private HL7OrganizerConvertor()
	{
	}

	public static Protocol toProtocol(REPCMT000100UV01Organizer organizer)
	{
		CD code = organizer.getCode();
		Protocol protocol = new Protocol();
		protocol.setIdentifier(code.getCodeSystem() + '.' + code.getCode());
		protocol.setName(code.getDisplayName());
		return protocol;
	}
}
