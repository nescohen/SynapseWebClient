package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.widget.lazyload.SupportsLazyLoadInterface;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsWidget;

public interface EntityBadgeView extends IsWidget, SupportsLazyLoadInterface {
	void setEntity(EntityHeader header);

	void showLoadError(String entityId);

	void setClickHandler(ClickHandler handler);

	void setIcon(IconType iconType);

	void showAddToDownloadList();

	void setAnnotations(String html);

	void setSize(String s);

	void setMd5(String s);

	void showPublicIcon();

	void showPrivateIcon();

	void showSharingSetIcon();

	void showHasWikiIcon();

	void showUnlinkIcon();

	void setError(String error);

	void setPresenter(Presenter p);

	String getFriendlySize(Long contentSize, boolean b);

	void setModifiedByUserBadgeClickHandler(ClickHandler handler);

	void showDiscussionThreadIcon();

	void showMinimalColumnSet();

	void setEntityNameWidthPx(int width);

	void clear();

	public interface Presenter {
		void onUnlink();

		void onAddToDownloadList();
	}

}
