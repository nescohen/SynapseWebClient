package org.sagebionetworks.web.client.widget.entity.browse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.widget.entity.EntityTreeItem;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanelSelectionModel;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ClientBundleWithLookup;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeImages;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.Tree.Resources;
import com.google.inject.Inject;

public class EntityTreeBrowserViewImpl extends FlowPanel implements EntityTreeBrowserView {

	private static final String PLACEHOLDER_ID = "-1";
	private static final String PLACEHOLDER_TYPE = "-1";
	public static final String PLACEHOLDER_NAME_PREFIX = "&#8212";
	
	private static final int TREE_ITEM_ICON_INDEX = 0;
	
	private Presenter presenter;
	private SageImageBundle sageImageBundle;
	private IconsImageBundle iconsImageBundle;
	private PortalGinInjector ginInjector;
		
	private boolean makeLinks = true;
	private Integer height = null;
	private Tree entityTree;
	private Map<TreeItem, EntityTreeItem> treeItem2entityTreeItem;
	private Set<EntityTreeItem> alreadyFetchedEntityChildren;

	@Inject
	public EntityTreeBrowserViewImpl(SageImageBundle sageImageBundle, IconsImageBundle iconsImageBundle, PortalGinInjector ginInjector) {
		this.sageImageBundle = sageImageBundle;
		this.iconsImageBundle = iconsImageBundle;
		this.ginInjector = ginInjector;
		
		treeItem2entityTreeItem = new HashMap<TreeItem, EntityTreeItem>();
		alreadyFetchedEntityChildren = new HashSet<EntityTreeItem>();
		
		entityTree = new Tree(new EntityTreeResources());
		
		this.add(entityTree);
		entityTree.addOpenHandler(new OpenHandler<TreeItem>() {
			
			/**
			 * When a node is expanded, if its children have not already
			 * been fetched and placed into the tree, it will delete the dummy
			 * child node and fetch the actual children of the expanded node.
			 * During this process, the icon of the expanded node is switched
			 * to a loading indicator.
			 */
			@Override
			public void onOpen(OpenEvent<TreeItem> event) {
				final EntityTreeItem target = treeItem2entityTreeItem.get(event.getTarget());
				if (!alreadyFetchedEntityChildren.contains(target)) {
					// We have not already fetched children for this entity.
					
					// Change to loading icon.
					target.showLoadingChildren();
					
					presenter.getFolderChildren(target.getHeader().getId(), new AsyncCallback<List<EntityHeader>>() {
						
						@Override
						public void onSuccess(List<EntityHeader> result) {
							// We got the children.
							alreadyFetchedEntityChildren.add(target);
							target.asTreeItem().removeItems();	// Remove the dummy item.
							
							// Make a tree item for each child and place them in the tree.
							for (EntityHeader header : result) {
								createAndPlaceTreeItem(header, target, false);
							}
							
							// Change back to type icon.
							target.showTypeIcon();
						}
						
						@Override
						public void onFailure(Throwable caught) {
							DisplayUtils.showErrorMessage("ERROROROROORROORR");	// TODO
						}
						
					});
				}
			}
			
		});
	}

	@Override
	public Widget asWidget() {
		return this;
	}	

	@Override 
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
		
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void clear() {
		entityTree.clear();
		treeItem2entityTreeItem.clear();
	}

	@Override
	public void setRootEntities(List<EntityHeader> rootEntities, boolean sort) {
		clear();
		
		if(rootEntities == null) rootEntities = new ArrayList<EntityHeader>();
		if(rootEntities.size() == 0) {
			EntityHeader eh = new EntityHeader();
			eh.setId(PLACEHOLDER_ID);
			eh.setName(PLACEHOLDER_NAME_PREFIX + " " + DisplayConstants.EMPTY);
			eh.setType(PLACEHOLDER_TYPE);
			rootEntities.add(eh);
		}
		for (final EntityHeader header : rootEntities) {
			createAndPlaceTreeItem(header, null, true);
		}
	}
	
	@Override
	public void setMakeLinks(boolean makeLinks) {
		this.makeLinks = makeLinks;
	}
	
	/*
	 * Private Methods
	 */
	
	private List<EntityTreeModel> convertEntityHeaderToModel(List<EntityHeader> headers) {
		
		List<EntityTreeModel> models = new ArrayList<EntityTreeModel>();
		int maxlimit = presenter.getMaxLimit();
		
		for(int i=0; i<headers.size() && i<maxlimit; i++) {
			EntityHeader header = headers.get(i);
			String link;
			String name = header.getName();
			if(makeLinks && !PLACEHOLDER_ID.equals(header.getId())) {
				link = "<a href=\"" + DisplayUtils.getSynapseHistoryToken(header.getId()) + "\">" + name + "</a>";
			} else {
				link = name;
			}
			models.add(new EntityTreeModel(header.getId(), name, link, header.getType()));
		}
		if(headers.size() >= maxlimit) {
			models.add(new EntityTreeModel(PLACEHOLDER_ID, PLACEHOLDER_ID, PLACEHOLDER_NAME_PREFIX + " Limited to " + maxlimit + " results" , PLACEHOLDER_TYPE));
		}
		return models;
	}

//	@Override
//	public void removeEntity(EntityHeader entityHeader) {
//		entityTree.remove(header2)
//	}

	@Override
	public void setWidgetHeight(int height) {
		this.height = height;
	}
	
	/**
	 * Makes a TreeItem and places it in the tree. Gives the created item a "dummy"
	 * child so that the item can be expanded.
	 * @param childToCreate The EntityHeader who's information will be used to create a
	 * 					 	new tree item and place in the tree.
	 * @param parent The EntityHeader that corresponds to the tree item the the created
	 * 				 child will become the child of. Parameter ignored if isRootItem.
	 * @param isRootItem true if the childToCreate is a root item, false otherwise.
	 */
	private void createAndPlaceTreeItem(final EntityHeader childToCreate, final EntityTreeItem parent, boolean isRootItem) {
		if (parent == null && !isRootItem) throw new IllegalArgumentException("Must specify a parent entity under which to place the created child in the tree.");
		
		if (PLACEHOLDER_TYPE.equals(childToCreate.getType())) {
			// Not an actual entity. Just display and call it good.
			TreeItem placeHolderItem = new TreeItem(new HTMLPanel("<div>" + childToCreate.getName() + "</div>"));
			if (isRootItem) {
				entityTree.addItem(placeHolderItem);
			} else {
				parent.asTreeItem().addItem(placeHolderItem);
			}
			return;
		}
		
		// Make tree item.
		EntityTreeItem childItem = ginInjector.getEntityTreeItemWidget();
		childItem.configure(childToCreate);
		
		// Update fields.
		treeItem2entityTreeItem.put(childItem.asTreeItem(), childItem);
				
		// HACKY: Add dummy item to childItem to make expandable.
		childItem.asTreeItem().addItem(createDummyItem());
		
		// Place the created child in the tree as the child of the given parent entity.
		if (isRootItem) {
			entityTree.addItem(childItem);
		} else {
			parent.asTreeItem().addItem(childItem);
		}
	}
	
	/**
	 * Creates the dummy item used to make all items placed in the tree
	 * expandable.
	 * @return the dummy tree item.
	 */
	private TreeItem createDummyItem() {
		TreeItem result = new TreeItem();
		result.setVisible(false);
		return result;
	}
	
	public class EntityTreeResources implements Tree.Resources {
		@Override
	    public ImageResource treeClosed() {
	        return EntityTreeImageBundle.IMAGE_RESOURCE.treeClosed();
	    }

	    @Override
	    public ImageResource treeOpen() {
	        return EntityTreeImageBundle.IMAGE_RESOURCE.treeOpen();
	    }

		@Override
		public ImageResource treeLeaf() {
			return EntityTreeImageBundle.DEFAULT_RESOURCES.treeLeaf();
		}
	}
	
	public interface EntityTreeImageBundle extends ClientBundle, ClientBundleWithLookup {
		EntityTreeImageBundle IMAGE_RESOURCE = GWT.create(EntityTreeImageBundle.class);
		Tree.Resources DEFAULT_RESOURCES = GWT.create(Tree.Resources.class);

		// TODO: Change paths to proper images.
	    @Source("images/icons/arrow-down-dir.png")
		ImageResource treeOpen();
		
	    
		@Source("images/icons/arrow-right-dir.png")
		ImageResource treeClosed();
	}
	
}
