/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.visualization.opengl.compatibility.objects;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeData;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;
import org.gephi.visualization.VizController;
import org.gephi.visualization.api.ModelImpl;
import org.gephi.visualization.gleem.linalg.Vec3f;
import org.gephi.visualization.gleem.linalg.Vecf;
import org.gephi.visualization.opengl.octree.Octant;

/**
 *
 * @author Mathieu Bastian
 */
public class Arrow3dModel extends ModelImpl<NodeData> {

    private static float ARROW_WIDTH = 1f;
    private static float ARROW_HEIGHT = 1.1f;
    private EdgeData edge;
    private float[] cameraLocation;

    private Arrow3dModel() {
        super();
        cameraLocation = VizController.getInstance().getDrawable().getCameraLocation();
        octants = new Octant[1];
    }

    public Arrow3dModel(EdgeData edge) {
        this();
        this.edge = edge;
    }

    @Override
    public void display(GL gl, GLU glu) {
        NodeData nodeFrom = edge.getSource();
        NodeData nodeTo = edge.getTarget();

        //Edge size
        float weight = edge.getEdge().getWeight();
        float arrowWidth = ARROW_WIDTH * weight * 2f;
        float arrowHeight = ARROW_HEIGHT * weight * 2f;

        //Edge vector
        Vec3f edgeVector = new Vec3f(nodeTo.x() - nodeFrom.x(), nodeTo.y() - nodeFrom.y(), nodeTo.z() - nodeFrom.z());
        edgeVector.normalize();

        //Get collision distance between nodeTo and arrow point
        double angle = Math.atan2(nodeTo.y() - nodeFrom.y(), nodeTo.x() - nodeFrom.x());
        float collisionDistance = ((ModelImpl) nodeTo.getModel()).getCollisionDistance(angle);

        float x2 = nodeTo.x();
        float y2 = nodeTo.y();
        float z2 = nodeTo.z();

        //Point of the arrow
        float targetX = x2 - edgeVector.x() * collisionDistance;
        float targetY = y2 - edgeVector.y() * collisionDistance;
        float targetZ = z2 - edgeVector.z() * collisionDistance;

        //Base of the arrow
        float baseX = targetX - edgeVector.x() * arrowHeight * 2f;
        float baseY = targetY - edgeVector.y() * arrowHeight * 2f;
        float baseZ = targetZ - edgeVector.z() * arrowHeight * 2f;

        //Camera vector
        Vec3f cameraVector = new Vec3f(targetX - cameraLocation[0], targetY - cameraLocation[1], targetZ - cameraLocation[2]);

        //Side vector
        Vec3f sideVector = edgeVector.cross(cameraVector);
        sideVector.normalize();

        //Draw the triangle
        gl.glColor4f(edge.r(), edge.g(), edge.b(), edge.alpha());
        gl.glVertex3d(baseX + sideVector.x() * arrowWidth, baseY + sideVector.y() * arrowWidth, baseZ + sideVector.z() * arrowWidth);
        gl.glVertex3d(baseX - sideVector.x() * arrowWidth, baseY - sideVector.y() * arrowWidth, baseZ - sideVector.z() * arrowWidth);
        gl.glVertex3d(targetX, targetY, targetZ);
    }

    @Override
    public float getCollisionDistance(double angle) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isInOctreeLeaf(Octant leaf) {
        return ((ModelImpl) obj.getModel()).getOctants()[0] == leaf;
    }

    @Override
    public int[] octreePosition(float centerX, float centerY, float centerZ,
            float size) {
        return null;
    }

    @Override
    public boolean selectionTest(Vecf distanceFromMouse, float selectionSize) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String toSVG() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setOctant(Octant octant) {
        this.octants[0] = octant;
    }

    @Override
    public Octant[] getOctants() {
        Octant[] oc = ((ModelImpl) obj.getModel()).getOctants();
        if (oc[0] == null) //The edge has been destroyed
        {
            oc = this.octants;
        }
        return oc;
    }

    @Override
    public boolean isCacheMatching(int cacheMarker) {
        if (edge.getModel() != null) {
            return ((ModelImpl) edge.getModel()).isCacheMatching(cacheMarker);
        }
        return false;
    }
}
