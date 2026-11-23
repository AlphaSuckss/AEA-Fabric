package dev.alphasucks.aea.port.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_3542;
import org.jetbrains.annotations.Nullable;

public enum Shape implements class_3542 {
   CUBE("cube"),
   SPHERE("sphere"),
   STAR("star"),
   CONE("cone"),
   CUBE_HOLLOW("cube_hollow"),
   SPHERE_HOLLOW("sphere_hollow"),
   STAR_HOLLOW("star_hollow"),
   CONE_HOLLOW("cone_hollow");

   public static final double DEFAULT_THICKNESS = 1.0;
   public static final Codec<Shape> CODEC = class_3542.method_28140(Shape::values);
   public static final Expression DEFAULT_HOLLOW_THICKNESS = Expression.constant(1.0);
   public static final MapCodec<Expression> HOLLOW_THICKNESS = Expression.DOUBLE_OR_EXPR.optionalFieldOf("hollow_thickness", DEFAULT_HOLLOW_THICKNESS);
   private final String name;

   Shape(String name) {
      this.name = name;
   }

   @Override
   public String method_15434() {
      return this.name;
   }

   public Shape solid() {
      return switch (this) {
         case CUBE_HOLLOW -> CUBE;
         case SPHERE_HOLLOW -> SPHERE;
         case STAR_HOLLOW -> STAR;
         case CONE_HOLLOW -> CONE;
         default -> this;
      };
   }

   public boolean hollow() {
      return this.solid() != this;
   }

   public double thickness(Expression thickness, @Nullable class_1297 entity) {
      return this.hollow() ? thickness.eval(entity) : 0.0;
   }

   public double thickness(Expression thickness, @Nullable class_1937 level) {
      return this.hollow() ? thickness.eval(level) : 0.0;
   }

   public boolean contains(int dx, int dy, int dz, int radius) {
      return this.contains(dx, dy, dz, radius, radius, radius);
   }

   public boolean contains(int dx, int dy, int dz, int rx, int ry, int rz) {
      return this.contains((double)dx, (double)dy, (double)dz, (double)rx, (double)ry, (double)rz, 1.0);
   }

   public boolean contains(int dx, int dy, int dz, int rx, int ry, int rz, double thickness) {
      return this.contains((double)dx, (double)dy, (double)dz, (double)rx, (double)ry, (double)rz, thickness);
   }

   public boolean contains(double dx, double dy, double dz, double rx, double ry, double rz) {
      return this.contains(dx, dy, dz, rx, ry, rz, 1.0);
   }

   public boolean contains(double dx, double dy, double dz, double rx, double ry, double rz, double thickness) {
      if (!this.containsSolid(dx, dy, dz, rx, ry, rz)) {
         return false;
      } else {
         return this.hollow() && !(this.depth(dx, dy, dz, rx, ry, rz) < thickness)
            ? this == SPHERE_HOLLOW && (rx != ry || ry != rz) && this.axisNearSurface(dx, dy, dz, rx, ry, rz, thickness)
            : true;
      }
   }

   private boolean axisNearSurface(double dx, double dy, double dz, double rx, double ry, double rz, double reach) {
      return !this.containsSolid(dx + reach, dy, dz, rx, ry, rz)
         || !this.containsSolid(dx - reach, dy, dz, rx, ry, rz)
         || !this.containsSolid(dx, dy + reach, dz, rx, ry, rz)
         || !this.containsSolid(dx, dy - reach, dz, rx, ry, rz)
         || !this.containsSolid(dx, dy, dz + reach, rx, ry, rz)
         || !this.containsSolid(dx, dy, dz - reach, rx, ry, rz);
   }

   private boolean containsSolid(double dx, double dy, double dz, double rx, double ry, double rz) {
      return switch (this) {
         case CUBE, CUBE_HOLLOW -> Math.abs(dx) <= rx && Math.abs(dy) <= ry && Math.abs(dz) <= rz;
         case SPHERE, SPHERE_HOLLOW -> {
            double nx = norm(dx, rx);
            double ny = norm(dy, ry);
            double nz = norm(dz, rz);
            yield nx * nx + ny * ny + nz * nz <= 1.0;
         }
         case STAR, STAR_HOLLOW -> {
            double nx = norm(dx, rx);
            double ny = norm(dy, ry);
            double nz = norm(dz, rz);
            yield nx + ny + nz <= 1.0;
         }
         case CONE, CONE_HOLLOW -> {
            if (!(dy < 0.0) && !(dy > ry)) {
               double t = ry <= 0.0 ? 1.0 : dy / ry;
               double nx = norm(dx, rx);
               double nz = norm(dz, rz);
               yield Math.sqrt(nx * nx + nz * nz) <= t;
            } else {
               yield false;
            }
         }
      };
   }

   private double depth(double dx, double dy, double dz, double rx, double ry, double rz) {
      if (!(rx <= 0.0) && !(ry <= 0.0) && !(rz <= 0.0)) {
         return switch (this) {
            case CUBE, CUBE_HOLLOW -> Math.min(rx - Math.abs(dx), Math.min(ry - Math.abs(dy), rz - Math.abs(dz)));
            case SPHERE, SPHERE_HOLLOW -> ellipsoidDepth(dx, dy, dz, rx, ry, rz);
            case STAR, STAR_HOLLOW -> octahedronDepth(dx, dy, dz, rx, ry, rz);
            case CONE, CONE_HOLLOW -> coneDepth(dx, dy, dz, rx, ry, rz);
         };
      } else {
         return 0.0;
      }
   }

   private static double ellipsoidDepth(double dx, double dy, double dz, double rx, double ry, double rz) {
      double ax = dx / rx;
      double ay = dy / ry;
      double az = dz / rz;
      double bx = ax / rx;
      double by = ay / ry;
      double bz = az / rz;
      double gradient = Math.sqrt(bx * bx + by * by + bz * bz);
      if (gradient == 0.0) {
         return Math.min(rx, Math.min(ry, rz));
      }

      double extent = Math.sqrt(ax * ax + ay * ay + az * az);
      return extent * (1.0 - extent) / gradient;
   }

   private static double octahedronDepth(double dx, double dy, double dz, double rx, double ry, double rz) {
      double extent = Math.abs(dx) / rx + Math.abs(dy) / ry + Math.abs(dz) / rz;
      double normal = Math.sqrt(1.0 / (rx * rx) + 1.0 / (ry * ry) + 1.0 / (rz * rz));
      return (1.0 - extent) / normal;
   }

   private static double coneDepth(double dx, double dy, double dz, double rx, double ry, double rz) {
      double ax = dx / rx;
      double az = dz / rz;
      double spread = Math.sqrt(ax * ax + az * az);
      double horizontal = Math.sqrt(dx * dx + dz * dz);
      double rim = horizontal > 0.0 ? horizontal / spread : Math.min(rx, rz);
      double wall = rim * (dy - ry * spread) / Math.sqrt(rim * rim + ry * ry);
      return Math.min(ry - dy, wall);
   }

   private static double norm(double delta, double radius) {
      if (radius <= 0.0) {
         return delta == 0.0 ? 0.0 : Double.POSITIVE_INFINITY;
      } else {
         return Math.abs(delta) / radius;
      }
   }

   public List<class_2338> positions(class_2338 center, int radius) {
      return this.positions(center, radius, radius, radius);
   }

   public List<class_2338> positions(class_2338 center, int rx, int ry, int rz) {
      return this.positions(center, rx, ry, rz, 1.0);
   }

   public List<class_2338> positions(class_2338 center, int rx, int ry, int rz, double thickness) {
      List<class_2338> out = new ArrayList();

      for (int dx = -rx; dx <= rx; dx++) {
         for (int dy = -ry; dy <= ry; dy++) {
            for (int dz = -rz; dz <= rz; dz++) {
               if (this.contains(dx, dy, dz, rx, ry, rz, thickness)) {
                  out.add(center.method_10069(dx, dy, dz));
               }
            }
         }
      }

      return out;
   }
}
