package dev.heeystuuh.primeva.client;

import dev.heeystuuh.primeva.PrimevaMod;
import dev.heeystuuh.primeva.entity.ArcaneGolem;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

/** Modelo do Golem Arcano: corpo atarracado, cabeça grande, braços longos. */
public class GolemModel extends HierarchicalModel<ArcaneGolem> implements net.minecraft.client.model.ArmedModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(PrimevaMod.id("arcane_golem"), "main");
    private final ModelPart root, head, rightArm, leftArm, rightLeg, leftLeg;

    public GolemModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    public static LayerDefinition create() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition p = mesh.getRoot();
        p.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4, -8, -4, 8, 8, 8), PartPose.offset(0, 6, 0));
        p.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 16).addBox(-4, 0, -3, 8, 10, 6), PartPose.offset(0, 6, 0));
        p.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(32, 0).addBox(-3, -1, -1.5f, 3, 11, 3), PartPose.offset(-4, 7, 0));
        p.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(44, 0).addBox(0, -1, -1.5f, 3, 11, 3), PartPose.offset(4, 7, 0));
        p.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(32, 16).addBox(-1.5f, 0, -1.5f, 3, 8, 3), PartPose.offset(-2, 16, 0));
        p.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(44, 16).addBox(-1.5f, 0, -1.5f, 3, 8, 3), PartPose.offset(2, 16, 0));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void translateToHand(net.minecraft.world.entity.HumanoidArm arm, com.mojang.blaze3d.vertex.PoseStack pose) {
        (arm == net.minecraft.world.entity.HumanoidArm.LEFT ? leftArm : rightArm).translateAndRotate(pose);
        pose.translate(arm == net.minecraft.world.entity.HumanoidArm.LEFT ? 0.06 : -0.06, 0.12, 0);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(ArcaneGolem e, float limbSwing, float limbAmount, float age, float yaw, float pitch) {
        head.yRot = yaw * Mth.DEG_TO_RAD;
        head.xRot = pitch * Mth.DEG_TO_RAD;
        float s = Mth.cos(limbSwing * 0.6662f) * 1.2f * limbAmount;
        rightLeg.xRot = s;
        leftLeg.xRot = -s;
        rightArm.xRot = -s * 0.8f;
        leftArm.xRot = s * 0.8f;
        float swing = e.getAttackAnim(age - e.tickCount);
        if (swing > 0) {
            rightArm.xRot = -2.0f + 1.6f * Mth.sin(swing * Mth.PI);
            leftArm.xRot = -2.0f + 1.6f * Mth.sin(swing * Mth.PI);
        }
        rightArm.zRot = 0.05f + Mth.sin(age * 0.067f) * 0.05f;
        leftArm.zRot = -rightArm.zRot;
    }
}
