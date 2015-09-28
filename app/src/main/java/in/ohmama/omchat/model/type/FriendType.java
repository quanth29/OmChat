package in.ohmama.omchat.model.type;

import org.jivesoftware.smack.packet.RosterPacket.ItemType;

/**
 * Created by yanglone on 9/21/15.
 */
public class FriendType {

    public static int getValue(ItemType itemType) {
        switch (itemType) {
            case none:
                return 0;
            case to:
                return 1;
            case from:
                return 2;
            case both:
                return 3;
            case remove:
                return 4;
            default:
                return 0;
        }
    }

    public static ItemType getType(int value) {
        switch (value) {
            case 0:
                return ItemType.none;
            case 1:
                return ItemType.to;
            case 2:
                return ItemType.from;
            case 3:
                return ItemType.both;
            case 4:
                return ItemType.remove;
            default:
                return ItemType.none;
        }
    }
}
