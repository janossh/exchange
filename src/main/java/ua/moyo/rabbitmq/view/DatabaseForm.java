package ua.moyo.rabbitmq.view;

import com.vaadin.data.converter.StringToIntegerConverter;
import com.vaadin.data.converter.StringToLongConverter;
import ua.moyo.rabbitmq.DashboardUI;
import ua.moyo.rabbitmq.model.Database;
import ua.moyo.rabbitmq.repository.DatabaseRepository;
import ua.moyo.rabbitmq.moyo.Events.DatabaseChangeEvent;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.*;
import org.vaadin.viritin.form.AbstractForm;
import org.vaadin.viritin.layouts.MVerticalLayout;

/**
 * Created by JLD on 05.06.2017.
 */
@UIScope
@SpringComponent
public class DatabaseForm extends AbstractForm<Database> {

    private static final long serialVersionUID = 1L;

    DatabaseRepository databaseRepository;
    final Button save;
    TextField name, ip, comment, maxConnection, id, rabbitqueue, base, user, password;
    CheckBox active, notShop;


    public DatabaseForm(DatabaseRepository databaseRepository) {
        super(Database.class);

        this.databaseRepository = databaseRepository;

        save = getSaveButton();
        save.setVisible(true);
        save.setCaption("���������");
        getResetButton().setVisible(true);
        getResetButton().setCaption("������");
        setSizeUndefined();


        name = new TextField("��� �������������� ����");
        ip = new TextField("IP �������������� ����");
        comment = new TextField("�����������");
        active = new CheckBox("���� �������");
        maxConnection = new TextField("������������ ���������� �����������");
        getBinder().forField(maxConnection).withConverter(new StringToIntegerConverter("������� �������� ��������")).bind("maxConnection");


        id = new TextField("��");
        getBinder().forField(id).withConverter(new StringToLongConverter("������� �������� ��������")).bind("id");

        notShop = new CheckBox("��� �� �������");
        getBinder().forField(notShop).bind("notShop");
        base = new TextField("��� ���� �� �������");
        user = new TextField("�����");
        password = new TextField("������");
        rabbitqueue = new TextField("��� ������� rabbitMQ");
        rabbitqueue.setDescription("���� �� ���������, ������� ����� ������������ �� ������������ ���������: "+
                        "��� ��������� ip ��������, ��� ������ ��� ����������_�������");
        setModalWindowTitle("���������� ����� ����");
        getResetButton().setCaption("������");

        name.addValueChangeListener(valueChangeEvent -> formHasChanged());
        rabbitqueue.addValueChangeListener(valueChangeEvent -> formHasChanged());
        ip.addValueChangeListener(valueChangeEvent -> formHasChanged());
        comment.addValueChangeListener(valueChangeEvent -> formHasChanged());
        active.addValueChangeListener(valueChangeEvent -> formHasChanged());
        maxConnection.addValueChangeListener(valueChangeEvent -> formHasChanged());
        id.addValueChangeListener(valueChangeEvent -> formHasChanged());
        notShop.addValueChangeListener(valueChangeEvent -> {
            formHasChanged();
            visibleNotShopDetails();

        });

        base.addValueChangeListener(valueChangeEvent -> formHasChanged());
        user.addValueChangeListener(valueChangeEvent -> formHasChanged());
        password.addValueChangeListener(valueChangeEvent -> formHasChanged());

        setSavedHandler(database -> {
            databaseRepository.saveAndFlush(database);
            DashboardUI.getDashboardEventbus().post(new DatabaseChangeEvent(database));
            closePopup();
        });

        visibleNotShopDetails();

    }

    private void visibleNotShopDetails() {
        boolean showDetails = notShop.getValue();
        base.setVisible(showDetails);
        user.setVisible(showDetails);
        password.setVisible(showDetails);
    }


    public void formHasChanged() {
    }

    @Override
    protected void bind() {
        super.bind();
    }

    @Override
    protected Component createContent() {
       return new MVerticalLayout(name, ip, comment, rabbitqueue, active,
               maxConnection, notShop, base, user, password, getToolbar()).withWidth("");
    }

    @Override
    public Window openInModalPopup() {
        return super.openInModalPopup();
    }
}
