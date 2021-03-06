package xfacthd.framedblocks;

import net.minecraft.block.Block;
import net.minecraft.item.*;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xfacthd.framedblocks.common.FBContent;
import xfacthd.framedblocks.common.block.IFramedBlock;
import xfacthd.framedblocks.common.net.OpenSignScreenPacket;
import xfacthd.framedblocks.common.net.SignUpdatePacket;

@Mod(FramedBlocks.MODID)
@Mod.EventBusSubscriber(modid = FramedBlocks.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class FramedBlocks
{
	public static final String MODID = "framedblocks";
	public static final Logger LOGGER = LogManager.getLogger();

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
	
    public static final ItemGroup FRAMED_GROUP = new ItemGroup("framed_blocks")
    {
        @Override
        public ItemStack createIcon() { return new ItemStack(FBContent.blockFramedCube); }

        @Override
        public void fill(NonNullList<ItemStack> items)
        {
            super.fill(items);
            items.sort((s1, s2) ->
            {
                if (s1.getItem() == FBContent.itemFramedHammer) { return 1; }
                if (s2.getItem() == FBContent.itemFramedHammer) { return -1; }

                Block b1 = ((BlockItem) s1.getItem()).getBlock();
                Block b2 = ((BlockItem) s2.getItem()).getBlock();
                return ((IFramedBlock)b1).getBlockType().compareTo(((IFramedBlock)b2).getBlockType());
            });
        }
    };

    @SubscribeEvent
    public static void setup(final FMLCommonSetupEvent event)
    {
        CHANNEL.messageBuilder(SignUpdatePacket.class, 0, NetworkDirection.PLAY_TO_SERVER)
                .encoder(SignUpdatePacket::encode)
                .decoder(SignUpdatePacket::new)
                .consumer(SignUpdatePacket::handle)
                .add();

        CHANNEL.messageBuilder(OpenSignScreenPacket.class, 1, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(OpenSignScreenPacket::encode)
                .decoder(OpenSignScreenPacket::new)
                .consumer(OpenSignScreenPacket::handle)
                .add();
    }
}
