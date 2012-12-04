package org.sagebionetworks.web.client.widget.entity.dialog.editors;

import org.sagebionetworks.web.client.widget.WidgetDescriptorView;

import com.google.gwt.user.client.ui.IsWidget;

public interface ProvenanceConfigView extends IsWidget, WidgetDescriptorView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	public void setEntityId(String entityId);
	public String getEntityId();
	public void setDepth(int depth);
	public int getDepth();
	public void setIsExpanded(boolean b);
	public boolean isExpanded();
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {

	}
}
