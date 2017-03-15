package org.sagebionetworks.web.unitclient.widget.accessrequirements;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.accessrequirements.ACTAccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.ACTAccessRequirementWidgetView;
import org.sagebionetworks.web.client.widget.accessrequirements.requestaccess.CreateDataAccessRequestWizard;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;

public class ACTAccessRequirementWidgetTest {
	ACTAccessRequirementWidget widget;
	@Mock
	ACTAccessRequirementWidgetView mockView; 
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	WikiPageWidget mockWikiPageWidget;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	CreateDataAccessRequestWizard mockCreateDataAccessRequestWizard;
	@Mock
	ACTAccessRequirement mockACTAccessRequirement;
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		widget = new ACTAccessRequirementWidget(mockView, mockSynapseClient, mockWikiPageWidget, mockSynAlert, mockGinInjector);
		when(mockGinInjector.getCreateDataAccessRequestWizard()).thenReturn(mockCreateDataAccessRequestWizard);
	}

	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(widget);
		verify(mockView).setWikiTermsWidget(any(Widget.class));
		verify(mockWikiPageWidget).setModifiedCreatedByVisible(false);
		verify(mockWikiPageWidget).showWikiHistory(false);
	}

	@Test
	public void testSetRequirementWithContactInfoTerms() {
		String tou = "must do things before access is allowed";
		when(mockACTAccessRequirement.getActContactInfo()).thenReturn(tou);
		widget.setRequirement(mockACTAccessRequirement);
		verify(mockView).setTerms(tou);
		verify(mockView).showTermsUI();
	}
	@Test
	public void testSetRequirementWithWikiTerms() {
		widget.setRequirement(mockACTAccessRequirement);
		verify(mockWikiPageWidget).configure(any(WikiPageKey.class), eq(false), any(WikiPageWidget.Callback.class), eq(false));
		verify(mockView, never()).setTerms(anyString());
		verify(mockView, never()).showTermsUI();
		
	}

}