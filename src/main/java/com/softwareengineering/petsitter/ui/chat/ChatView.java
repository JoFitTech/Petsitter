package com.softwareengineering.petsitter.ui.chat;

import com.softwareengineering.petsitter.chat.service.ChatService;
import com.softwareengineering.petsitter.ui.shared.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "chat", layout = MainLayout.class)
@PageTitle("Chat | Pawsitter")
@PermitAll
public class ChatView extends VerticalLayout {

    private static final String DARK     = "#4a3428";
    private static final String CREAM    = "#fbf8f1";
    private static final String CARD_BG  = "#ffffff";

    public ChatView(ChatService chatService) {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("position", "relative")
                  .set("overflow", "hidden");

        // Decorative circles
        add(buildDecorativeCircles());

        // Main content wrapper
        VerticalLayout mainContent = new VerticalLayout();
        mainContent.setWidthFull();
        mainContent.setPadding(false);
        mainContent.setSpacing(false);
        mainContent.getStyle()
            .set("padding", "48px 48px 64px 48px")
            .set("position", "relative")
            .set("z-index", "1")
            .set("box-sizing", "border-box")
            .set("max-width", "1200px")
            .set("margin", "0 auto");

        mainContent.add(buildPageHeader());
        mainContent.add(buildChatContainer());

        add(mainContent);
    }

    private Component buildDecorativeCircles() {
        Div circles = new Div();
        circles.getStyle()
            .set("position", "absolute")
            .set("top", "0").set("left", "0")
            .set("width", "100%").set("height", "100%")
            .set("pointer-events", "none")
            .set("z-index", "0");

        // Left beige circle
        Div leftCircle = new Div();
        leftCircle.getStyle()
            .set("position", "absolute")
            .set("width", "400px")
            .set("height", "400px")
            .set("border-radius", "50%")
            .set("background", "#e8ddd4")
            .set("opacity", "0.6")
            .set("top", "-100px")
            .set("left", "-100px");

        // Right mint circle
        Div rightCircle = new Div();
        rightCircle.getStyle()
            .set("position", "absolute")
            .set("width", "600px")
            .set("height", "600px")
            .set("border-radius", "50%")
            .set("background", "#c8dde6")
            .set("opacity", "0.6")
            .set("top", "-200px")
            .set("right", "-150px");

        circles.add(leftCircle, rightCircle);
        return circles;
    }

    private Component buildPageHeader() {
        Div wrapper = new Div();
        wrapper.getStyle()
            .set("margin-bottom", "32px");

        H1 title = new H1("Inbox");
        title.getStyle()
            .set("margin", "0 0 6px 0")
            .set("font-size", "28px")
            .set("font-weight", "800")
            .set("color", DARK);

        Paragraph subtitle = new Paragraph("Verwalte hier deine eingehenden Nachrichten und behalte alle wichtigen Unterhaltungen im Blick.");
        subtitle.getStyle()
            .set("margin", "0")
            .set("font-size", "14px")
            .set("color", "#7a6050");

        wrapper.add(title, subtitle);
        return wrapper;
    }

    private Component buildChatContainer() {
        HorizontalLayout container = new HorizontalLayout();
        container.setWidthFull();
        container.setHeight("650px"); // Fixed height for the chat area
        container.setSpacing(false);
        container.setPadding(false);
        container.getStyle()
            .set("background", CARD_BG)
            .set("border-radius", "20px")
            .set("box-shadow", "0 8px 32px rgba(74,52,40,0.09)")
            .set("overflow", "hidden");

        // Left sidebar (Chat list)
        VerticalLayout chatList = new VerticalLayout();
        chatList.setWidth("320px");
        chatList.setHeightFull();
        chatList.setPadding(false);
        chatList.setSpacing(false);
        chatList.getStyle()
            .set("border-right", "1px solid #f0e6da")
            .set("overflow-y", "auto")
            .set("background", "#ffffff")
            .set("padding", "20px 16px");

        // Add dummy chat list items
        chatList.add(chatListItem("max3010", "★★★★★", "2 Hunde", true, true));
        chatList.add(chatListItem("lis20", "★★★", "2 Skorpione", false, true));
        chatList.add(chatListItem("lada000", "★★★★", "29 Jahre", false, false));
        chatList.add(chatListItem("xxisax", "★★★★★", "21 Jahre", false, false));
        chatList.add(chatListItem("melanie20", "★★★★", "1 Katze", false, true));

        // Right main area (Chat window)
        VerticalLayout chatWindow = new VerticalLayout();
        chatWindow.setWidthFull();
        chatWindow.setHeightFull();
        chatWindow.setPadding(false);
        chatWindow.setSpacing(false);
        chatWindow.getStyle().set("padding", "24px 32px");
        
        chatWindow.add(buildChatHeader("max3010"));
        
        VerticalLayout messagesArea = buildMessagesArea();
        chatWindow.add(messagesArea);
        chatWindow.setFlexGrow(1, messagesArea);

        chatWindow.add(buildMessageInput());

        container.add(chatList, chatWindow);
        return container;
    }

    private Component chatListItem(String name, String stars, String secondaryInfo, boolean selected, boolean isSitter) {
        Div item = new Div();
        item.getStyle()
            .set("padding", "14px 16px")
            .set("border-radius", "12px")
            .set("margin-bottom", "10px")
            .set("cursor", "pointer")
            .set("display", "flex")
            .set("gap", "14px")
            .set("align-items", "center");

        if (selected) {
            item.getStyle().set("background", "#ebd7c0"); // darker beige
        } else {
            item.getStyle().set("background", "#fcfaf6") // very light beige
                         .set("border", "1px solid #f0e6da");
        }

        // Avatar
        Div avatar = createAvatar(44);

        // Content
        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);
        
        HorizontalLayout topRow = new HorizontalLayout();
        topRow.setWidthFull();
        topRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        topRow.setAlignItems(FlexComponent.Alignment.CENTER);
        
        Span nameSpan = new Span(name);
        nameSpan.getStyle().set("font-weight", "700").set("color", DARK).set("font-size", "15px");
        
        Span starsSpan = new Span(stars);
        starsSpan.getStyle().set("color", "#f5c842").set("font-size", "11px").set("letter-spacing", "1px");
        
        topRow.add(nameSpan, starsSpan);

        HorizontalLayout bottomRow = new HorizontalLayout();
        bottomRow.setWidthFull();
        bottomRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        bottomRow.setAlignItems(FlexComponent.Alignment.CENTER);
        bottomRow.getStyle().set("margin-top", "6px");
        
        Span infoSpan = new Span(secondaryInfo);
        infoSpan.getStyle().set("color", "#7a6050").set("font-size", "13px");
        
        Icon typeIcon = isSitter ? new Icon(VaadinIcon.HEART) : new Icon(VaadinIcon.HOME);
        typeIcon.setSize("12px");
        typeIcon.getStyle().set("color", "#a08060");
        
        bottomRow.add(infoSpan, typeIcon);
        
        content.add(topRow, bottomRow);
        item.add(avatar, content);

        item.addClickListener(e -> System.out.println("Chat item clicked: " + name));

        return item;
    }

    private Div createAvatar(int size) {
        Div avatar = new Div();
        avatar.getStyle()
            .set("width", size + "px")
            .set("height", size + "px")
            .set("min-width", size + "px")
            .set("border-radius", "50%")
            .set("background", "#8db3c3") // blue matching the user view avatar color
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("overflow", "hidden");

        Div svgWrap = new Div();
        svgWrap.getElement().setProperty("innerHTML",
            "<svg width='" + (size*0.6) + "' height='" + (size*0.6) + "' viewBox='0 0 24 24' fill='none' xmlns='http://www.w3.org/2000/svg'>" +
            "<circle cx='12' cy='8' r='4' fill='white'/>" +
            "<path d='M4 20c0-4 3.6-7 8-7s8 3 8 7' fill='white'/></svg>");
        avatar.add(svgWrap);
        return avatar;
    }

    private Component buildChatHeader(String name) {
        VerticalLayout headerWrap = new VerticalLayout();
        headerWrap.setPadding(false);
        headerWrap.setSpacing(false);
        headerWrap.setWidthFull();

        HorizontalLayout header = new HorizontalLayout();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.getStyle().set("gap", "16px").set("padding-bottom", "16px");

        header.add(createAvatar(48));

        H2 nameHeader = new H2(name);
        nameHeader.getStyle().set("margin", "0").set("font-size", "28px").set("font-weight", "800").set("color", DARK);
        header.add(nameHeader);

        Hr divider = new Hr();
        divider.getStyle().set("margin", "0").set("border-color", "#e8ddd4").set("width", "100%");

        headerWrap.add(header, divider);
        return headerWrap;
    }

    private VerticalLayout buildMessagesArea() {
        VerticalLayout messagesArea = new VerticalLayout();
        messagesArea.setWidthFull();
        messagesArea.setPadding(false);
        messagesArea.getStyle()
            .set("overflow-y", "auto")
            .set("padding", "32px 0")
            .set("gap", "40px");

        // Top right message
        HorizontalLayout rightMsgRow = new HorizontalLayout();
        rightMsgRow.setWidthFull();
        rightMsgRow.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        
        Div rightBubbleWrap = new Div();
        rightBubbleWrap.getStyle().set("position", "relative");
        
        Div rightBubble = new Div();
        rightBubble.getStyle()
            .set("background", "#f7f5f0")
            .set("border", "1px solid #e8ddd4")
            .set("border-radius", "16px")
            .set("padding", "20px")
            .set("width", "360px")
            .set("height", "80px");
            
        Div rightAvatar = createAvatar(36);
        rightAvatar.getStyle()
            .set("position", "absolute")
            .set("bottom", "-18px")
            .set("right", "-18px")
            .set("border", "4px solid #ffffff");
            
        rightBubbleWrap.add(rightBubble, rightAvatar);
        rightMsgRow.add(rightBubbleWrap);

        // Bottom left message
        HorizontalLayout leftMsgRow = new HorizontalLayout();
        leftMsgRow.setWidthFull();
        leftMsgRow.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        
        Div leftBubbleWrap = new Div();
        leftBubbleWrap.getStyle().set("position", "relative").set("margin-top", "12px");
        
        Div leftBubble = new Div();
        leftBubble.getStyle()
            .set("background", "#ebd7c0")
            .set("border-radius", "16px")
            .set("padding", "20px")
            .set("width", "360px")
            .set("height", "80px");
            
        Div leftAvatar = createAvatar(36);
        leftAvatar.getStyle()
            .set("position", "absolute")
            .set("bottom", "-18px")
            .set("left", "-18px")
            .set("border", "4px solid #ffffff");
            
        leftBubbleWrap.add(leftBubble, leftAvatar);
        leftMsgRow.add(leftBubbleWrap);

        messagesArea.add(rightMsgRow, leftMsgRow);
        return messagesArea;
    }

    private Component buildMessageInput() {
        HorizontalLayout inputArea = new HorizontalLayout();
        inputArea.setWidthFull();
        inputArea.setAlignItems(FlexComponent.Alignment.CENTER);
        inputArea.getStyle()
            .set("background", "#fcfaf6")
            .set("border", "1px solid #e8ddd4")
            .set("border-radius", "12px")
            .set("padding", "14px 20px")
            .set("margin-top", "16px")
            .set("gap", "16px");

        Icon cameraIcon = new Icon(VaadinIcon.CAMERA);
        cameraIcon.getStyle().set("color", "#a08060").set("cursor", "pointer");
        cameraIcon.setSize("24px");
        cameraIcon.addClickListener(e -> System.out.println("Camera icon clicked"));

        Input messageInput = new Input();
        messageInput.setPlaceholder("Schreibe hier deine Nachricht");
        messageInput.getStyle()
            .set("flex", "1")
            .set("border", "none")
            .set("background", "transparent")
            .set("outline", "none")
            .set("font-size", "15px")
            .set("color", DARK)
            .set("font-family", "Inter, Arial, sans-serif");
            
        Icon sendIcon = new Icon(VaadinIcon.PAPERPLANE_O);
        sendIcon.getStyle().set("color", "#4a3428").set("cursor", "pointer");
        sendIcon.setSize("24px");
        sendIcon.addClickListener(e -> {
            System.out.println("Send clicked, message: " + messageInput.getValue());
            messageInput.setValue("");
        });

        inputArea.add(cameraIcon, messageInput, sendIcon);

        return inputArea;
    }
}

