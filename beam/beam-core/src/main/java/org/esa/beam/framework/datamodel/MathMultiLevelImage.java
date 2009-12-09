package org.esa.beam.framework.datamodel;

import com.bc.ceres.glevel.MultiLevelImage;
import com.bc.ceres.glevel.MultiLevelModel;
import com.bc.ceres.glevel.MultiLevelSource;
import com.bc.ceres.glevel.support.AbstractMultiLevelSource;
import com.bc.ceres.glevel.support.DefaultMultiLevelImage;
import com.bc.jexp.ParseException;
import org.esa.beam.framework.dataop.barithm.BandArithmetic;
import org.esa.beam.jai.ImageManager;
import org.esa.beam.jai.ResolutionLevel;
import org.esa.beam.jai.VirtualBandOpImage;

import java.awt.image.RenderedImage;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * A {@link MultiLevelImage} computed from raster data arithmetics. The {@link MathMultiLevelImage}
 * resets itsself whenever any referred raster data have changed.
 *
 * @author Ralf Quast
 * @version $Revision: 1.4 $ $Date: 2009-12-09 16:41:39 $
 * @since BEAM 4.7
 */
class MathMultiLevelImage extends DefaultMultiLevelImage implements ProductNodeListener {

    private final Map<Product, Set<ProductNode>> nodeMap = new WeakHashMap<Product, Set<ProductNode>>();

    /**
     * Creates a new mask {@link MultiLevelImage} computed from raster data arithmetics. The mask
     * image created is reset whenever any referred raster data have changed.
     * <p/>
     * A 'node data changed' event is fired from the associated {@link RasterDataNode} whenever
     * the mask image is reset.
     *
     * @param expression     the raster data arithmetic expression.
     * @param associatedNode the {@link RasterDataNode} associated with the image being created.
     *
     * @return the {@code MultiLevelImage} created.
     */
    static MultiLevelImage createMask(final String expression, final RasterDataNode associatedNode) {
        final MultiLevelModel multiLevelModel = ImageManager.getMultiLevelModel(associatedNode);
        final MultiLevelSource multiLevelSource = new AbstractMultiLevelSource(multiLevelModel) {
            @Override
            public RenderedImage createImage(int level) {
                return VirtualBandOpImage.createMask(expression,
                                                     associatedNode.getProduct(),
                                                     ResolutionLevel.create(getModel(), level));
            }
        };
        return new MathMultiLevelImage(multiLevelSource, expression, associatedNode.getProduct()) {
            @Override
            public void reset() {
                super.reset();
                associatedNode.fireProductNodeDataChanged();
            }
        };
    }

    /**
     * Creates a new {@link MultiLevelImage} computed from raster data arithmetics. The image
     * created is reset whenever any referred raster data have changed.
     * <p/>
     * A 'node data changed' event is fired from the associated {@link RasterDataNode} whenever
     * the image created is reset.
     *
     * @param expression     the raster data arithmetic expression.
     * @param associatedNode the {@link RasterDataNode} associated with the image being created.
     *
     * @return the {@code MultiLevelImage} created.
     */
    static MultiLevelImage create(final String expression, final RasterDataNode associatedNode) {
        final MultiLevelModel multiLevelModel = ImageManager.getMultiLevelModel(associatedNode);
        final MultiLevelSource multiLevelSource = new AbstractMultiLevelSource(multiLevelModel) {
            @Override
            public RenderedImage createImage(int level) {
                return VirtualBandOpImage.create(expression,
                                                 associatedNode.getDataType(),
                                                 associatedNode.isNoDataValueUsed() ? associatedNode.getGeophysicalNoDataValue() : null,
                                                 associatedNode.getProduct(),
                                                 ResolutionLevel.create(getModel(), level));
            }
        };
        return new MathMultiLevelImage(multiLevelSource, expression, associatedNode.getProduct()) {
            @Override
            public void reset() {
                super.reset();
                associatedNode.fireProductNodeDataChanged();
            }
        };
    }

    /**
     * Creates a new {@link MultiLevelImage} computed from raster data arithmetics. The created
     * image resets itsself whenever any referred raster data have changed.
     *
     * @param multiLevelSource the multi-level image source
     * @param expression       the raster data arithmetic expression.
     * @param product          the parent of the raster data node(s) referred in {@code expression}.
     */
    MathMultiLevelImage(MultiLevelSource multiLevelSource, String expression, Product product) {
        super(multiLevelSource);
        try {
            final RasterDataNode[] nodes;
            final ProductManager productManager = product.getProductManager();
            if (productManager != null) {
                nodes = BandArithmetic.getRefRasters(expression,
                                                     productManager.getProducts(),
                                                     productManager.getProductIndex(product));
            } else {
                nodes = BandArithmetic.getRefRasters(expression, product);
            }
            if (nodes.length > 0) {
                for (final RasterDataNode node : nodes) {
                    if (!nodeMap.containsKey(node.getProduct())) {
                        nodeMap.put(node.getProduct(), new WeakHashSet<ProductNode>());
                    }
                    nodeMap.get(node.getProduct()).add(node);
                }
                for (final Product key : nodeMap.keySet()) {
                    key.addProductNodeListener(this);
                }
            }
        } catch (ParseException e) {
            // ignore, we do not need to listen to raster data nodes
        }
    }

    @Override
    public void dispose() {
        for (final Product key : nodeMap.keySet()) {
            key.removeProductNodeListener(this);
        }
        nodeMap.clear();
        super.dispose();
    }

    @Override
    public void nodeChanged(ProductNodeEvent event) {
    }

    @Override
    public void nodeDataChanged(ProductNodeEvent event) {
        final Product product = event.getSourceNode().getProduct();
        if (nodeMap.containsKey(product)) {
            if (nodeMap.get(product).contains(event.getSourceNode())) {
                reset();
            }
        }
    }

    @Override
    public void nodeAdded(ProductNodeEvent event) {
    }

    @Override
    public void nodeRemoved(ProductNodeEvent event) {
    }

    // use for testing only
    Map<Product, Set<ProductNode>> getNodeMap() {
        return nodeMap;
    }

    // implementation copied from {@code HashSet}
    private static class WeakHashSet<E> extends AbstractSet<E> {

        private static final Object PRESENT = new Object();
        private final WeakHashMap<E, Object> map;

        private WeakHashSet() {
            map = new WeakHashMap<E, Object>();
        }

        @Override
        public Iterator<E> iterator() {
            return map.keySet().iterator();
        }

        @Override
        public int size() {
            return map.size();
        }

        @Override
        public boolean isEmpty() {
            return map.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            //noinspection SuspiciousMethodCalls
            return map.containsKey(o);
        }

        @Override
        public boolean add(E e) {
            return map.put(e, PRESENT) == null;
        }

        @Override
        public boolean remove(Object o) {
            return map.remove(o) == PRESENT;
        }

        @Override
        public void clear() {
            map.clear();
        }
    }
}
