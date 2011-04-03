// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.craftbook.cart;

import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

public class CartBooster extends CartMechanism {
    public CartBooster(double multiplier) {
        super();
        this.multiplier = multiplier;
        this.newVelocity = newVelocity;
    }

    private final double multiplier;
    private Vector newVelocity;

    public void impact(Minecart cart, Block entered, Block from) {
        if (multiplier > 1) {
            newVelocity = cart.getVelocity().normalize().multiply(multiplier);
        } else if (multiplier < 1) {
            newVelocity = cart.getVelocity().multiply(multiplier);
        } else if (multiplier == 1) {
            return;
        }
        cart.setVelocity(newVelocity);
    }
}
