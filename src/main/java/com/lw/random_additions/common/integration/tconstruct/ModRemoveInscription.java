package com.lw.random_additions.common.integration.tconstruct;

import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.mantle.client.book.BookTransformer;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.PageData;
import slimeknights.mantle.client.book.data.SectionData;
import slimeknights.mantle.client.book.data.element.TextData;
import slimeknights.mantle.util.RecipeMatch;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.book.TinkerBook;
import slimeknights.tconstruct.library.book.content.ContentListing;
import slimeknights.tconstruct.library.book.content.ContentModifier;
import slimeknights.tconstruct.library.modifiers.IModifier;
import slimeknights.tconstruct.library.modifiers.IModifierDisplay;
import slimeknights.tconstruct.library.modifiers.IToolMod;
import slimeknights.tconstruct.library.modifiers.ModifierNBT;
import slimeknights.tconstruct.library.modifiers.TinkerGuiException;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.TinkerUtil;

import java.util.*;

public class ModRemoveInscription implements IModifier, IModifierDisplay {

    public static final String IDENTIFIER = "remove_inscription";
    private static final String EXTRA_TRAIT_PREFIX = "extratrait";
    private static final int COLOR = 0x8B4513;

    private final List<ItemStack> recipeItems = new ArrayList<>();

    public ModRemoveInscription() {
        TinkerRegistry.registerModifier(this);
    }

    public void addRecipeItem(ItemStack stack) {
        recipeItems.add(stack);
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public String getLocalizedName() {
        return Util.translate("modifier.%s.name", IDENTIFIER);
    }

    @Override
    public String getLocalizedDesc() {
        return Util.translate("modifier.%s.desc", IDENTIFIER);
    }

    @Override
    public List<String> getExtraInfo(ItemStack tool, NBTTagCompound modifierTag) {
        return Collections.emptyList();
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public boolean canApplyTogether(IToolMod otherModifier) {
        return true;
    }

    @Override
    public boolean canApplyTogether(Enchantment enchantment) {
        return true;
    }
    @Override
    public Optional<RecipeMatch.Match> matches(NonNullList<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                for (ItemStack item : recipeItems) {
                    if (stack.getItem() == item.getItem()) {
                        ItemStack single = stack.copy();
                        single.setCount(1);
                        return Optional.of(new RecipeMatch.Match(
                                NonNullList.withSize(1, single), 1));
                    }
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean canApply(ItemStack stack, ItemStack original) {
        NBTTagCompound root = TagUtil.getTagSafe(stack);
        NBTTagList modifiers = TagUtil.getBaseModifiersTagList(root);
        for (int i = 0; i < modifiers.tagCount(); i++) {
            if (modifiers.getStringTagAt(i).contains(EXTRA_TRAIT_PREFIX)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void apply(ItemStack stack) {
        NBTTagCompound root = TagUtil.getTagSafe(stack);
        apply(root);
        stack.setTagCompound(root);
    }

    @Override
    public void apply(NBTTagCompound root) {
        if (!TinkerUtil.hasModifier(root, IDENTIFIER)) {
            NBTTagList tagList = TagUtil.getBaseModifiersTagList(root);
            tagList.appendTag(new NBTTagString(IDENTIFIER));
            TagUtil.setBaseModifiersTagList(root, tagList);
        }

        NBTTagCompound modifierTag = new NBTTagCompound();
        NBTTagList modifiersTag = TagUtil.getModifiersTagList(root);
        int index = TinkerUtil.getIndexInList(modifiersTag, IDENTIFIER);
        if (index >= 0) {
            modifierTag = modifiersTag.getCompoundTagAt(index);
        }

        ModifierNBT data = ModifierNBT.readTag(modifierTag);
        data.identifier = IDENTIFIER;
        data.color = COLOR;
        data.level++;
        data.write(modifierTag);

        if (index >= 0) {
            modifiersTag.set(index, modifierTag);
        } else {
            modifiersTag.appendTag(modifierTag);
        }
        TagUtil.setModifiersTagList(root, modifiersTag);

        applyEffect(root, modifierTag);
    }

    @Override
    public void updateNBT(NBTTagCompound modifierTag) {
    }

    @Override
    public void applyEffect(NBTTagCompound rootCompound, NBTTagCompound modifierTag) {
        NBTTagList baseModifiers = TagUtil.getBaseModifiersTagList(rootCompound);
        NBTTagList newBaseModifiers = new NBTTagList();
        for (int i = 0; i < baseModifiers.tagCount(); i++) {
            String id = baseModifiers.getStringTagAt(i);
            if (!id.contains(EXTRA_TRAIT_PREFIX) && !id.equals(IDENTIFIER)) {
                newBaseModifiers.appendTag(new NBTTagString(id));
            }
        }
        TagUtil.setBaseModifiersTagList(rootCompound, newBaseModifiers);

        NBTTagList modifiersTag = TagUtil.getModifiersTagList(rootCompound);
        NBTTagList newModifiersTag = new NBTTagList();
        for (int i = 0; i < modifiersTag.tagCount(); i++) {
            NBTTagCompound tag = modifiersTag.getCompoundTagAt(i);
            String id = tag.getString("identifier");
            if (!id.contains(EXTRA_TRAIT_PREFIX) && !id.equals(IDENTIFIER)) {
                newModifiersTag.appendTag(tag);
            }
        }
        TagUtil.setModifiersTagList(rootCompound, newModifiersTag);
    }

    @Override
    public String getTooltip(NBTTagCompound modifierTag, boolean detailed) {
        ModifierNBT data = ModifierNBT.readTag(modifierTag);
        String name = getLocalizedName();
        if (data.level > 1) {
            name += " " + TinkerUtil.getRomanNumeral(data.level);
        }
        return name;
    }

    @Override
    public boolean hasTexturePerMaterial() {
        return false;
    }

    @Override
    public boolean equalModifier(NBTTagCompound modifierTag1, NBTTagCompound modifierTag2) {
        ModifierNBT data1 = ModifierNBT.readTag(modifierTag1);
        ModifierNBT data2 = ModifierNBT.readTag(modifierTag2);
        return data1.identifier.equals(data2.identifier);
    }

    @Override
    public boolean hasItemsToApplyWith() {
        return !recipeItems.isEmpty();
    }

    @Override
    public int getColor() {
        return COLOR;
    }

    @Override
    public List<List<ItemStack>> getItems() {
        List<List<ItemStack>> result = new ArrayList<>();
        for (ItemStack stack : recipeItems) {
            result.add(Collections.singletonList(stack));
        }
        return result;
    }

    @SideOnly(Side.CLIENT)
    public static void addTConstructBookEntry() {
        if (Loader.isModLoaded("tconstruct")) {

            TinkerBook.INSTANCE.addTransformer(new BookTransformer() {
                @Override
                public void transform(BookData book) {
                    for (SectionData section : book.sections) {
                        if (!"modifiers".equals(section.name)) continue;

                        for (PageData existing : section.pages) {
                            if (existing.content instanceof ContentModifier) {
                                if (ModRemoveInscription.IDENTIFIER.equals(
                                        ((ContentModifier) existing.content).modifierName)) {
                                    return;
                                }
                            }
                        }

                        ContentModifier content = new ContentModifier();
                        content.modifierName = ModRemoveInscription.IDENTIFIER;
                        content.text = new TextData[]{
                                new TextData(I18n.format("modifier.remove_inscription.book.text"))
                        };
                        content.effects = new String[]{
                                I18n.format("modifier.remove_inscription.book.effect1"),
                                I18n.format("modifier.remove_inscription.book.effect2"),
                        };

                        PageData page = new PageData(true);
                        page.name = ModRemoveInscription.IDENTIFIER;
                        page.source = section.source;
                        page.parent = section;
                        page.content = content;
                        page.load();

                        section.pages.add(page);

                        if (!section.pages.isEmpty() && section.pages.get(0).content instanceof ContentListing) {
                            IModifier modifier = TinkerRegistry.getModifier(ModRemoveInscription.IDENTIFIER);
                            if (modifier != null) {
                                ((ContentListing) section.pages.get(0).content)
                                        .addEntry(modifier.getLocalizedName(), page);
                            }
                        }
                        break;
                    }
                }
            });

        }
    }
}
