package org.sagebionetworks.web.client.view;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Lead;
import org.sagebionetworks.web.client.widget.header.Header;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ErrorViewImpl implements ErrorView {
	@UiField
	Anchor goBackLink;
	@UiField
	Lead message;
	private Header headerWidget;
	
	public interface Binder extends UiBinder<Widget, ErrorViewImpl> {}
	public Widget widget;
	
	@Inject
	public ErrorViewImpl(Binder uiBinder,
			Header headerWidget) {
		widget = uiBinder.createAndBindUi(this);
		this.headerWidget = headerWidget;
		headerWidget.configure();
		goBackLink.addClickHandler(event -> History.back());
	}

	@Override
	public void refreshHeader() {
		headerWidget.configure();
		headerWidget.refresh();
		com.google.gwt.user.client.Window.scrollTo(0, 0); // scroll user to top of page
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setErrorMessage(String error) {
		message.setText(error);
	}
}
