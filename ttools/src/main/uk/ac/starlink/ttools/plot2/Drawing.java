package uk.ac.starlink.ttools.plot2;

import uk.ac.starlink.ttools.plot2.data.DataStore;
import uk.ac.starlink.ttools.plot2.paper.Paper;

/**
 * Does the work of drawing graphics onto a given Paper object.
 *
 * <p>The drawing operation is separated into two parts:
 * calculating the Plan, and using that plan to do the drawing.
 * There are two purposes to this.
 * First, calculating the plan is a thread-safe operation, 
 * while the actual drawing may not be done at the same time as other
 * drawing operations on the same Paper object, so calculating plans
 * separately offers opportunities for concurrency which would not be
 * available if drawing was monolithic.
 * Second, the plan may be reusable between different Drawing instances.
 * Calling code may cache plans so that they do not need to be calculated
 * every time if changes to a plot are made which do not affect a given 
 * drawing (for instance, other layers are changed).
 *
 * <p>It is worthwhile for a Drawing implementation to perform this
 * split into two parts only if the plan calculation is a time-consuming
 * operation that may conveniently be separated from actual drawing.
 * If not, it's OK to return null from {@link #calculatePlan}.
 *
 * <p>As a general rule, we calculate plans where the work required
 * scales with dataset size (number of points plotted), but not where
 * it scales with plot size (number of pixels on screen).
 * The plan/plot mechanism is really intended to cope with potentially
 * rather slow plots (millions of rows) rather than to make normal
 * plots run extra fast.  But you can break this rule if you like.
 *
 * @author   Mark Taylor
 * @since    11 Feb 2013
 */
public interface Drawing {

    /**
     * Performs preparation for the actual drawing.
     * Calling this method has no side effects, and it may be called in
     * any thread without concurrency implications.
     * If separate plan calculation is not useful, it's OK to return null.
     *
     * <p>The <code>knownPlans</code> argument may offer a selection of
     * pre-calculated plans that the calling code may have cached.
     * Implementations may examine these to see whether one of them
     * is the answer to the question being asked, and if so return it
     * without further work.
     *
     * @param  knownPlans  list of zero or more plans that may have been
     *                     previously calculated by this class
     * @param  dataStore   data-bearing object
     * @return  plan to present to the <code>paintData</code> method
     */
    @Slow
    Object calculatePlan( Object[] knownPlans, DataStore dataStore );

    /**
     * Performs the actual drawing.  The <code>plan</code> argument must
     * be the result of an earlier call to this object's
     * {@link #calculatePlan} method using the same data store.
     * The <code>paper</code> argument must in general be of a particular
     * type, according to how this drawing was generated.
     * Usually, this drawing object will own a
     * {@link uk.ac.starlink.ttools.plot2.paper.PaperType} instance
     * which can be used to paint to the supplied <code>paper</code> object.
     *
     * <p>This method must not be called concurrently with other objects
     * drawing to the same paper.
     *
     * @param  plan    drawing plan, from <code>calculatePlan</code>
     * @param  paper   graphics destination
     * @param  dataStore  data-bearing object
     */
    @Slow
    void paintData( Object plan, Paper paper, DataStore dataStore );
}
