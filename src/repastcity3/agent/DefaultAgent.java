/*
ęCopyright 2012 Nick Malleson
This file is part of RepastCity.

RepastCity is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

RepastCity is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with RepastCity.  If not, see <http://www.gnu.org/licenses/>.
 */

package repastcity3.agent;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import repast.simphony.dataLoader.ContextCreator;
import repastcity3.environment.Building;
import repastcity3.environment.Route;
import repastcity3.main.ContextManager;

public class DefaultAgent implements IAgent {

	private static Logger LOGGER = Logger.getLogger(DefaultAgent.class.getName());

	private Building home; // Where the agent lives
	private Building workplace; // Where the agent works
	private Route route; // An object to move the agent around the world

	private boolean goingHome = false; // Whether the agent is going to or from their home

	private static int uniqueID = 0;
	private int id;

	public DefaultAgent() {
		this.id = uniqueID++;
		// Find a building that agents can use as their workplace. First, iterate over all buildings in the model
		for (Building b : ContextManager.buildingContext.getRandomObjects(Building.class, 10000)) {
			// See if the building is a bank (they will have type==2).
			if (b.getType() == 2) {
				this.workplace = b;
				break; // Have found a bank, stop searching.
			}
		}
		assert this.workplace != null;
	}
 
	@Override
	public void step() throws Exception {

		// See what the time is, this will determine what the agent should be doing. The BigDecimal stuff
		// is just to round the time to 5 decimal places, otherwise it will never be exactly 9.0 or 17.0.
		double theTime = BigDecimal.valueOf(ContextManager.realTime).round(new MathContext(5,RoundingMode.HALF_UP)).doubleValue();
		
		if (theTime == 9.0) { // 9am, Agent should be working			
			this.route = new Route(this, this.workplace.getCoords(), this.workplace); // Create a route to work
		} 
		else if (theTime == 17.0) { // 5pm, agent should go home
			this.route = new Route(this, this.home.getCoords(), this.home); // Create a route home
		}

		if (this.route == null) {
			// Don't do anything if a route hasn't been created.
		} else if (this.route.atDestination()) {
			// Have reached our destination, lets delete the old route (more efficient).
			this.route = null;
		}
		else {
			// Otherwise travel towards the destination
			this.route.travel();
		}

		// Default agent behaviour, either go home or go to a random house
		/*
		 * LOGGER.log(Level.FINE, "Agent " + this.id + " is stepping."); if (this.route == null) { // route can only be
		 * null when the simulation starts, so the agent must be leaving home this.goingHome = false; // Choose a new
		 * building to go to Building b = ContextManager.buildingContext.getRandomObject(); this.route = new Route(this,
		 * b.getCoords(), b); LOGGER.log(Level.FINE, this.toString() + " created new route to " + b.toString()); } if
		 * (!this.route.atDestination()) { this.route.travel(); LOGGER.log(Level.FINE, this.toString() +
		 * " travelling to " + this.route.getDestinationBuilding().toString()); } else { // Have reached destination,
		 * now either go home or onto another building if (this.goingHome) { this.goingHome = false; Building b =
		 * ContextManager.buildingContext.getRandomObject(); this.route = new Route(this, b.getCoords(), b);
		 * LOGGER.log(Level.FINE, this.toString() + " reached home, now going to " + b.toString()); } else {
		 * LOGGER.log(Level.FINE, this.toString() + " reached " + this.route.getDestinationBuilding().toString() +
		 * ", now going home"); this.goingHome = true; this.route = new Route(this, this.home.getCoords(), this.home); }
		 * 
		 * }
		 */

	} // step()

	/**
	 * There will be no inter-agent communication so these agents can be executed simulataneously in separate threads.
	 */
	@Override
	public final boolean isThreadable() {
		return true;
	}

	@Override
	public void setHome(Building home) {
		this.home = home;
	}

	@Override
	public Building getHome() {
		return this.home;
	}

	@Override
	public <T> void addToMemory(List<T> objects, Class<T> clazz) {
	}

	@Override
	public List<String> getTransportAvailable() {
		return null;
	}

	@Override
	public String toString() {
		return "Agent " + this.id;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof DefaultAgent))
			return false;
		DefaultAgent b = (DefaultAgent) obj;
		return this.id == b.id;
	}

	@Override
	public int hashCode() {
		return this.id;
	}
}
