package org.alfresco.university.platformsample;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.util.List;

public class Rating
        implements NodeServicePolicies.OnDeleteNodePolicy,
        NodeServicePolicies.OnCreateNodePolicy {
    // SNIP

    // Dependencies
    private NodeService nodeService;
    private PolicyComponent policyComponent;

    // Behaviours
    private Behaviour onCreateNode;
    private Behaviour onDeleteNode;

    public void init() {

        // Create behaviours
        this.onCreateNode = new JavaBehaviour(this, "onCreateNode", Behaviour.NotificationFrequency.EVERY_EVENT);

        this.onDeleteNode = new JavaBehaviour(this, "onDeleteNode", Behaviour.NotificationFrequency.EVERY_EVENT);

        // Bind behaviours to node policies
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateNode"),
                QName.createQName(SomeCoRatingsModel.NAMESPACE_SOMECO_RATINGS_CONTENT_MODEL, SomeCoRatingsModel.TYPE_SCR_RATING),
                this.onCreateNode
        );

        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onDeleteNode"),
                QName.createQName(SomeCoRatingsModel.NAMESPACE_SOMECO_RATINGS_CONTENT_MODEL, SomeCoRatingsModel.TYPE_SCR_RATING),
                this.onDeleteNode
        );

    }
    public void onCreateNode(ChildAssociationRef childAssocRef) {

        computeAverage(childAssocRef);

    }

    public void onDeleteNode(ChildAssociationRef childAssocRef, boolean isNodeArchived) {

        computeAverage(childAssocRef);

    }

    public void computeAverage(ChildAssociationRef childAssocRef) {

        // get the parent node
        NodeRef parentRef = childAssocRef.getParentRef();

        // check the parent to make sure it has the right aspect
        if (nodeService.exists(parentRef) && nodeService.hasAspect(parentRef, QName.createQName(SomeCoRatingsModel.NAMESPACE_SOMECO_RATINGS_CONTENT_MODEL, SomeCoRatingsModel.ASPECT_SCR_RATEABLE))) {

            // continue, this is what we want

        } else {

            return;

        }

        // get the parent node's children
        List<ChildAssociationRef> children = nodeService.getChildAssocs(parentRef);

        // iterate through the children to compute the total
        Double average = 0d;
        int total = 0;
        for (ChildAssociationRef child : children) {
            int rating = (Integer)nodeService.getProperty(
                    child.getChildRef(),
                    QName.createQName(SomeCoRatingsModel.NAMESPACE_SOMECO_RATINGS_CONTENT_MODEL, SomeCoRatingsModel.PROP_RATING));
            total += rating;
        }

        // compute the average
        average = total / (children.size() / 1.0d);

        // store the average, total, count on the parent node
        nodeService.setProperty(
                parentRef,
                QName.createQName(
                        SomeCoRatingsModel.NAMESPACE_SOMECO_RATINGS_CONTENT_MODEL,
                        SomeCoRatingsModel.PROP_AVERAGE_RATING),
                average);

        nodeService.setProperty(
                parentRef,
                QName.createQName(
                        SomeCoRatingsModel.NAMESPACE_SOMECO_RATINGS_CONTENT_MODEL,
                        SomeCoRatingsModel.PROP_TOTAL_RATING),
                total);

        Double count = 0d;
        nodeService.setProperty(
                parentRef,
                QName.createQName(
                        SomeCoRatingsModel.NAMESPACE_SOMECO_RATINGS_CONTENT_MODEL,
                        SomeCoRatingsModel.PROP_RATING_COUNT),
                count);

        return;

    }
}
