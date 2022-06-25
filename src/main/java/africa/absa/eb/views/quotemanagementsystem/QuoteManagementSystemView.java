package africa.absa.eb.views.quotemanagementsystem;

import africa.absa.eb.data.entity.SentEmailAudit;
import africa.absa.eb.data.service.SentEmailAuditService;
import africa.absa.eb.views.MainLayout;
import com.vaadin.collaborationengine.CollaborationAvatarGroup;
import com.vaadin.collaborationengine.CollaborationBinder;
import com.vaadin.collaborationengine.UserInfo;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

@PageTitle("Quote Management System")
@Route(value = "/:sentEmailAuditID?/:action?(edit)", layout = MainLayout.class)
@PermitAll
public class QuoteManagementSystemView extends Div implements BeforeEnterObserver {

    private final String SENTEMAILAUDIT_ID = "sentEmailAuditID";
    private final String SENTEMAILAUDIT_EDIT_ROUTE_TEMPLATE = "/%s/edit";

    private Grid<SentEmailAudit> grid = new Grid<>(SentEmailAudit.class, false);

    CollaborationAvatarGroup avatarGroup;

    private TextField quote_number;
    private TextField ab_number;
    private TextField destination_email;
    private DateTimePicker sent_datetime;

    private Button cancel = new Button("Cancel");
    private Button save = new Button("Save");

    private CollaborationBinder<SentEmailAudit> binder;

    private SentEmailAudit sentEmailAudit;

    private final SentEmailAuditService sentEmailAuditService;

    @Autowired
    public QuoteManagementSystemView(SentEmailAuditService sentEmailAuditService) {
        this.sentEmailAuditService = sentEmailAuditService;
        addClassNames("quote-management-system-view");

        // UserInfo is used by Collaboration Engine and is used to share details
        // of users to each other to able collaboration. Replace this with
        // information about the actual user that is logged, providing a user
        // identifier, and the user's real name. You can also provide the users
        // avatar by passing an url to the image as a third parameter, or by
        // configuring an `ImageProvider` to `avatarGroup`.
        UserInfo userInfo = new UserInfo(UUID.randomUUID().toString(), "Steve Lange");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        avatarGroup = new CollaborationAvatarGroup(userInfo, null);
        avatarGroup.getStyle().set("visibility", "hidden");

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn("quote_number").setAutoWidth(true);
        grid.addColumn("ab_number").setAutoWidth(true);
        grid.addColumn("destination_email").setAutoWidth(true);
        grid.addColumn("sent_datetime").setAutoWidth(true);
        grid.setItems(query -> sentEmailAuditService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(SENTEMAILAUDIT_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(QuoteManagementSystemView.class);
            }
        });

        // Configure Form
        binder = new CollaborationBinder<>(SentEmailAudit.class, userInfo);

        // Bind fields. This is where you'd define e.g. validation rules
        binder.forField(quote_number, String.class)
                .withConverter(new StringToIntegerConverter("Only numbers are allowed")).bind("quote_number");

        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.sentEmailAudit == null) {
                    this.sentEmailAudit = new SentEmailAudit();
                }
                binder.writeBean(this.sentEmailAudit);

                sentEmailAuditService.update(this.sentEmailAudit);
                clearForm();
                refreshGrid();
                Notification.show("SentEmailAudit details stored.");
                UI.getCurrent().navigate(QuoteManagementSystemView.class);
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the sentEmailAudit details.");
            }
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<UUID> sentEmailAuditId = event.getRouteParameters().get(SENTEMAILAUDIT_ID).map(UUID::fromString);
        if (sentEmailAuditId.isPresent()) {
            Optional<SentEmailAudit> sentEmailAuditFromBackend = sentEmailAuditService.get(sentEmailAuditId.get());
            if (sentEmailAuditFromBackend.isPresent()) {
                populateForm(sentEmailAuditFromBackend.get());
            } else {
                Notification.show(
                        String.format("The requested sentEmailAudit was not found, ID = %d", sentEmailAuditId.get()),
                        3000, Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(QuoteManagementSystemView.class);
            }
        }
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        quote_number = new TextField("Quote_number");
        ab_number = new TextField("Ab_number");
        destination_email = new TextField("Destination_email");
        sent_datetime = new DateTimePicker("Sent_datetime");
        sent_datetime.setStep(Duration.ofSeconds(1));
        Component[] fields = new Component[]{quote_number, ab_number, destination_email, sent_datetime};

        formLayout.add(fields);
        editorDiv.add(avatarGroup, formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save, cancel);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getLazyDataView().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(SentEmailAudit value) {
        this.sentEmailAudit = value;
        String topic = null;
        if (this.sentEmailAudit != null && this.sentEmailAudit.getId() != null) {
            topic = "sentEmailAudit/" + this.sentEmailAudit.getId();
            avatarGroup.getStyle().set("visibility", "visible");
        } else {
            avatarGroup.getStyle().set("visibility", "hidden");
        }
        binder.setTopic(topic, () -> this.sentEmailAudit);
        avatarGroup.setTopic(topic);

    }
}