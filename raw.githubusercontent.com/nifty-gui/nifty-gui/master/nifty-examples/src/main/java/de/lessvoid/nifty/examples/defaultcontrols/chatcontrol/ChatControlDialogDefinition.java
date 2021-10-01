package de.lessvoid.nifty.examples.defaultcontrols.chatcontrol;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.builder.ControlBuilder;
import de.lessvoid.nifty.builder.ControlDefinitionBuilder;
import de.lessvoid.nifty.controls.chatcontrol.builder.ChatBuilder;
import de.lessvoid.nifty.examples.defaultcontrols.common.DialogPanelControlDefinition;

import javax.annotation.Nonnull;

/**
 * The ChatControlDialogRegister registers a new control (the whole ChatControlDialog) with
 * Nifty. We can later simply generate the whole dialog using a control with the given NAME.
 * 
 * @author void
 */
public class ChatControlDialogDefinition {
  @Nonnull
  public static final String NAME = "chatControlDialogControl";

  public static void register(@Nonnull final Nifty nifty) {
    new ControlDefinitionBuilder(NAME) {{
      controller(new ChatControlDialogController());
      control(new ControlBuilder(DialogPanelControlDefinition.NAME) {{
        control(new ChatBuilder("chat", 13) {{
          sendLabel("Send Message");
        }});
      }});
    }}.registerControlDefintion(nifty);
  }
}
