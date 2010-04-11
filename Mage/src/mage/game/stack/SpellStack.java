/*
* Copyright 2010 BetaSteward_at_googlemail.com. All rights reserved.
*
* Redistribution and use in source and binary forms, with or without modification, are
* permitted provided that the following conditions are met:
*
*    1. Redistributions of source code must retain the above copyright notice, this list of
*       conditions and the following disclaimer.
*
*    2. Redistributions in binary form must reproduce the above copyright notice, this list
*       of conditions and the following disclaimer in the documentation and/or other materials
*       provided with the distribution.
*
* THIS SOFTWARE IS PROVIDED BY BetaSteward_at_googlemail.com ``AS IS'' AND ANY EXPRESS OR IMPLIED
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
* FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL BetaSteward_at_googlemail.com OR
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
* SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
* ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
* ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
* The views and conclusions contained in the software and documentation are those of the
* authors and should not be interpreted as representing official policies, either expressed
* or implied, of BetaSteward_at_googlemail.com.
*/

package mage.game.stack;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.UUID;
import mage.Constants.Zone;
import mage.abilities.Ability;
import mage.abilities.effects.Effect;
import mage.abilities.effects.ReplacementEffect;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.players.Player;

/**
 *
 * @author BetaSteward_at_googlemail.com
 */
public class SpellStack extends Stack<StackObject> {

	//resolve top StackObject
	public void resolve(Game game) {
		StackObject top = this.peek();
		top.resolve(game);
		this.remove(top);
	}

	public void checkTriggers(GameEvent event, Game game) {
		for (StackObject stackObject: this) {
			stackObject.checkTriggers(event, game);
		}
	}


	public boolean counter(UUID objectId, UUID sourceId, Game game) {
		StackObject stackObject = getStackObject(objectId);
		if (stackObject != null) {
			if (!game.replaceEvent(GameEvent.getEvent(GameEvent.EventType.COUNTER, objectId, sourceId, stackObject.getControllerId()))) {
				this.remove(stackObject);
				stackObject.counter(game);
				game.fireEvent(GameEvent.getEvent(GameEvent.EventType.COUNTERED, objectId, sourceId, stackObject.getControllerId()));
				return true;
			}
			return false;
		}
		return false;
	}

	public boolean replaceEvent(GameEvent event, Game game) {
		boolean caught = false;
		List<ReplacementEffect> rEffects = new ArrayList<ReplacementEffect>();
		for (StackObject stackObject: this) {
			for (Ability ability: stackObject.getAbilities()) {
				if (ability.getZone() == Zone.STACK) {
					for (Effect effect: ability.getEffects()) {
						if (effect instanceof ReplacementEffect) {
							if (((ReplacementEffect)effect).applies(event, game))
								rEffects.add((ReplacementEffect) effect);
						}
					}
				}
			}
		}
		if (rEffects.size() > 0) {
			if (rEffects.size() == 1) {
				caught = rEffects.get(0).replaceEvent(event, game);
			}
			else {
				Player player = game.getPlayer(event.getPlayerId());
				caught = rEffects.get(player.chooseEffect(rEffects, game)).replaceEvent(event, game);
			}
		}
		return caught;
	}


	public StackObject getStackObject(UUID id) {
		for (StackObject stackObject: this) {
			if (stackObject.getId().equals(id))
				return stackObject;
		}
		return null;
	}
}
