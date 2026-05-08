package com.softwareengineering.petsitter.ui.chat;

import com.softwareengineering.petsitter.chat.service.ChatService;
import com.softwareengineering.petsitter.ui.shared.MainLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "chat", layout = MainLayout.class)
@PageTitle("Chat | Petsitter")
@PermitAll
public class ChatView extends VerticalLayout {

    public ChatView(ChatService chatService) {
        add(new H2("Meine Nachrichten"));
        add(new Paragraph("Hier können Sie bald mit anderen Nutzern chatten."));
        
        if (chatService.getMessages().isEmpty()) {
            add(new Paragraph("Keine neuen Nachrichten."));
        }
    }
}
