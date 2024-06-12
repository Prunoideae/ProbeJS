package moe.wolfgirl.probejs.util.special_docs;

import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.jdoc.document.DocumentClass;
import moe.wolfgirl.probejs.jdoc.document.DocumentMethod;
import moe.wolfgirl.probejs.jdoc.java.ClassInfo;
import moe.wolfgirl.probejs.jdoc.property.PropertyHide;
import moe.wolfgirl.probejs.jdoc.property.PropertyParam;
import moe.wolfgirl.probejs.jdoc.property.PropertyType;
import moe.wolfgirl.probejs.specials.special.recipe.component.ComponentConverter;
import dev.latvian.mods.kubejs.block.entity.BlockEntityAttachmentType;
import dev.latvian.mods.kubejs.block.entity.BlockEntityInfo;

import java.util.List;
import java.util.Map;

public class BlockEntityInfoDocument {
    public static void loadBlockEntityInfoDocument(List<DocumentClass> globalClasses) throws NoSuchMethodException {
        String clazzName = ClassInfo.getOrCache(BlockEntityInfo.class).getName();
        DocumentClass blockEntityInfo = globalClasses.stream().filter(documentClass -> documentClass.getName().equals(clazzName)).findAny().get();
        DocumentMethod attach = blockEntityInfo.methods.stream().filter(documentMethod -> documentMethod.getName().equals("attach")).findFirst().get();
        for (Map.Entry<String, BlockEntityAttachmentType> entry : BlockEntityAttachmentType.ALL.get().entrySet()) {
            String type = entry.getKey();
            BlockEntityAttachmentType attachmentDesc = entry.getValue();
            DocumentMethod method = attach.copy();
            method.params.set(0, new PropertyParam("type", new PropertyType.Native(ProbeJS.GSON.toJson(type)), false));
            method.params.set(1, new PropertyParam("input", ComponentConverter.fromDescription(attachmentDesc.input()), false));
            blockEntityInfo.methods.add(method);
        }
        attach.addProperty(new PropertyHide());
    }
}
