package org.jboss.interceptor.util.proxy;

import javassist.util.proxy.MethodHandler;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * @author Marius Bogoevici
 */
public abstract class TargetInstanceProxyMethodHandler<T> implements MethodHandler, Serializable
{
   private T targetInstance;

   private Class<? extends T> targetClass;

   public TargetInstanceProxyMethodHandler(T targetInstance, Class<? extends T> targetClass)
   {
      this.targetInstance = targetInstance;
      this.targetClass = targetClass;
   }

   public final Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable
   {
      if (thisMethod.getDeclaringClass().equals(TargetInstanceProxy.class))
      {
         if (thisMethod.getName().equals("getTargetInstance"))
         {
            return this.getTargetInstance();
         }
         else if (thisMethod.getName().equals("getTargetClass"))
         {
            return this.getTargetClass();
         }
         else
         {
            // we shouldn't arrive here
            return null;
         }
      }
      else
      {
         return doInvoke(self, thisMethod, proceed, args);
      }
   }

   protected abstract Object doInvoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable;

   public T getTargetInstance()
   {
      return targetInstance;
   }

   public Class<? extends T> getTargetClass()
   {
      return targetClass;
   }
}