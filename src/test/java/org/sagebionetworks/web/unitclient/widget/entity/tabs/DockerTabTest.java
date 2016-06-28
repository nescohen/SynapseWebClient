package org.sagebionetworks.web.unitclient.widget.entity.tabs;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.docker.DockerRepository;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.breadcrumb.Breadcrumb;
import org.sagebionetworks.web.client.widget.docker.DockerRepoListWidget;
import org.sagebionetworks.web.client.widget.docker.DockerRepoWidget;
import org.sagebionetworks.web.client.widget.entity.controller.StuAlert;
import org.sagebionetworks.web.client.widget.entity.tabs.DockerTab;
import org.sagebionetworks.web.client.widget.entity.tabs.DockerTabView;
import org.sagebionetworks.web.client.widget.entity.tabs.Tab;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class DockerTabTest {
	@Mock
	Tab mockTab;
	@Mock
	DockerTabView mockView;
	@Mock
	DockerRepoListWidget mockDockerRepoListWidget;
	@Mock
	DockerRepoWidget mockDockerRepoWidget;
	@Mock
	Breadcrumb mockBreadcrumb;
	@Mock
	StuAlert mockSynAlert;
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	SynapseClientAsync mockSynapseClient;

	@Mock
	CallbackP<Tab> mockOnClickCallback;
	@Mock
	CallbackP<Boolean> mockShowProjectInfoCallback;
	@Mock
	UserEntityPermissions mockPermissions;
	@Mock
	EntityBundle mockProjectEntityBundle;
	@Mock
	EntityUpdatedHandler mockEntityUpdatedHandler;
	@Mock
	DockerRepository mockDockerRepoEntity;
	@Mock
	Project mockProjectEntity;
	@Mock
	Throwable mockProjectBundleLoadError;
	@Mock
	EntityBundle mockDockerRepoEntityBundle;
	@Mock
	EntityPath mockPath;

	DockerTab tab;

	String projectEntityId = "syn123";
	String projectName = "test project";
	String dockerRepoEntityId = "syn170";
	String dockerRepoName = "test repository";

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		tab = new DockerTab(mockView, mockTab, mockDockerRepoListWidget,
				mockBreadcrumb, mockGinInjector,
				mockSynapseClient, mockSynAlert);
		when(mockProjectEntityBundle.getEntity()).thenReturn(mockProjectEntity);
		when(mockProjectEntityBundle.getPermissions()).thenReturn(mockPermissions);
		when(mockProjectEntity.getName()).thenReturn(projectName);
		when(mockDockerRepoEntity.getId()).thenReturn(dockerRepoEntityId);
		when(mockDockerRepoEntity.getName()).thenReturn(dockerRepoName);
		when(mockDockerRepoEntityBundle.getEntity()).thenReturn(mockDockerRepoEntity);
		when(mockGinInjector.createNewDockerRepoWidget()).thenReturn(mockDockerRepoWidget);
		when(mockDockerRepoEntityBundle.getPath()).thenReturn(mockPath);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(tab);
		verify(mockView).setBreadcrumb(any(Widget.class));
		verify(mockView).setDockerRepoList(any(Widget.class));
		verify(mockView).setSynapseAlert(any(Widget.class));
		verify(mockTab).configure(anyString(), any(Widget.class));
		verify(mockBreadcrumb).setLinkClickedHandler(any(CallbackP.class));
		verify(mockDockerRepoListWidget).setRepoClickedCallback(any(CallbackP.class));
	}

	@Test
	public void testAsTab() {
		assertEquals(mockTab, tab.asTab());
	}

	@Test
	public void testSetTabClickedCallback() {
		tab.setTabClickedCallback(mockOnClickCallback);
		verify(mockTab).addTabClickedCallback(mockOnClickCallback);
	}

	@Test
	public void testConfigureWithProject() {
		String areaToken = null;
		tab.setShowProjectInfoCallback(mockShowProjectInfoCallback);
		tab.setProject(projectEntityId, mockProjectEntityBundle, null);
		tab.configure(mockProjectEntity, mockEntityUpdatedHandler, areaToken);
		verify(mockSynAlert, atLeastOnce()).clear();
		verify(mockTab).setEntityNameAndPlace(eq(projectName), any(Synapse.class));
		verify(mockView).setBreadcrumbVisible(false);
		verify(mockView).setDockerRepoListVisible(true);
		verify(mockView).clearDockerRepoWidget();
		verify(mockShowProjectInfoCallback).invoke(true);
		verify(mockDockerRepoListWidget).configure(mockProjectEntityBundle);
	}

	@Test
	public void testConfigureWithProjectFailure() {
		String areaToken = null;
		mockProjectEntityBundle = null;
		tab.setShowProjectInfoCallback(mockShowProjectInfoCallback);
		tab.setProject(projectEntityId, mockProjectEntityBundle, mockProjectBundleLoadError);
		tab.configure(mockProjectEntity, mockEntityUpdatedHandler, areaToken);
		verify(mockSynAlert, atLeastOnce()).clear();
		verify(mockTab).setEntityNameAndPlace(eq(projectEntityId), any(Synapse.class));
		verify(mockDockerRepoListWidget, never()).configure(mockProjectEntityBundle);
		verify(mockSynAlert).handleException(mockProjectBundleLoadError);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureWithDockerRepoEntity() {
		String areaToken = null;
		tab.setShowProjectInfoCallback(mockShowProjectInfoCallback);
		tab.setProject(projectEntityId, mockProjectEntityBundle, mockProjectBundleLoadError);
		tab.configure(mockDockerRepoEntity, mockEntityUpdatedHandler, areaToken);
		verify(mockSynAlert, atLeastOnce()).clear();
		verify(mockSynapseClient).getEntityBundle(eq(dockerRepoEntityId), anyInt(), any(AsyncCallback.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureWithDockerRepoEntitySuccess() {
		String areaToken = null;
		AsyncMockStubber.callSuccessWith(mockDockerRepoEntityBundle)
				.when(mockSynapseClient)
				.getEntityBundle(eq(dockerRepoEntityId), anyInt(), any(AsyncCallback.class));
		tab.setShowProjectInfoCallback(mockShowProjectInfoCallback);
		tab.setProject(projectEntityId, mockProjectEntityBundle, mockProjectBundleLoadError);
		tab.configure(mockDockerRepoEntity, mockEntityUpdatedHandler, areaToken);
		verify(mockTab).setEntityNameAndPlace(eq(dockerRepoName), any(Synapse.class));
		verify(mockTab).showTab();
		verify(mockView).setBreadcrumbVisible(true);
		verify(mockView).setDockerRepoListVisible(false);
		verify(mockView).setDockerRepoWidgetVisible(true);
		verify(mockView, atLeastOnce()).clearDockerRepoWidget();
		verify(mockShowProjectInfoCallback).invoke(false);
		verify(mockBreadcrumb).configure(mockPath, EntityArea.DOCKER);
		verify(mockGinInjector).createNewDockerRepoWidget();
		verify(mockView).setDockerRepoWidget(any(Widget.class));
		verify(mockDockerRepoListWidget, never()).configure(mockProjectEntityBundle);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureWithDockerRepoEntityFailure() {
		String areaToken = null;
		AsyncMockStubber.callFailureWith(new Throwable())
				.when(mockSynapseClient)
				.getEntityBundle(eq(dockerRepoEntityId), anyInt(), any(AsyncCallback.class));
		tab.setShowProjectInfoCallback(mockShowProjectInfoCallback);
		tab.setProject(projectEntityId, mockProjectEntityBundle, mockProjectBundleLoadError);
		tab.configure(mockDockerRepoEntity, mockEntityUpdatedHandler, areaToken);
		verify(mockTab).setEntityNameAndPlace(eq(dockerRepoEntityId), any(Synapse.class));
		verify(mockTab).showTab();
		verify(mockSynAlert).handleException(any(Throwable.class));
		verify(mockBreadcrumb, never()).configure(mockPath, EntityArea.DOCKER);
		verify(mockGinInjector, never()).createNewDockerRepoWidget();
		verify(mockView, never()).setDockerRepoWidget(any(Widget.class));
		verify(mockDockerRepoListWidget, never()).configure(mockProjectEntityBundle);
	}

	@Test
	public void testShowError() {
		tab.setShowProjectInfoCallback(mockShowProjectInfoCallback);
		Throwable error = new Throwable();
		tab.showError(error );
		verify(mockSynAlert).clear();
		verify(mockSynAlert).handleException(error);
	}

	@Test
	public void testResetView() {
		tab.setShowProjectInfoCallback(mockShowProjectInfoCallback);
		tab.resetView();
		verify(mockView).setBreadcrumbVisible(false);
		verify(mockView).setDockerRepoListVisible(false);
		verify(mockView).setDockerRepoWidgetVisible(false);
		verify(mockView).clearDockerRepoWidget();
		verify(mockShowProjectInfoCallback).invoke(false);
		verify(mockSynAlert, never()).handleException(any(Throwable.class));
	}
}